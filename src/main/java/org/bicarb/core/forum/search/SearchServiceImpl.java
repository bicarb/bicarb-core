/*
 * Copyright (c) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bicarb.core.forum.search;

import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Query;
import org.bicarb.core.forum.domain.Post;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SearchServiceImpl.
 *
 * @author olOwOlo
 */
@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

  private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

  private static final int BATCH_SIZE = 20;

  private final AtomicBoolean indexing = new AtomicBoolean(false);

  private final EntityManager entityManager;
  private final Elide elide;

  @Autowired
  public SearchServiceImpl(EntityManager entityManager, Elide elide) {
    this.entityManager = entityManager;
    this.elide = elide;
  }

  @Override
  public ElideResponse search(String query, Pageable pageable, Principal auth) {
    logger.debug("search: [query: {}, pageable: {}]", query, pageable);

    Pair<List<Integer>, Integer> idAndSize = searchIdByLuceneQuery(builder -> builder
        .simpleQueryString()
        .onField("topic.title").boostedTo(2f)
        .andField("cooked")
        .matching(query)
        .createQuery(), pageable);

    return getElideResponseBySearchResult(idAndSize, auth);
  }

  @Override
  public ElideResponse searchMoreLikeThis(Integer postId, Pageable pageable, Principal auth) {
    logger.debug("searchMoreLikeThis: [postId: {}, pageable: {}]", postId, pageable);
    Pair<List<Integer>, Integer> idAndSize = searchIdByLuceneQuery(builder -> builder
        .moreLikeThis()
        .excludeEntityUsedForComparison()
        .comparingField("topic.title").boostedTo(2f)
        .andField("cooked")
        .toEntityWithId(postId)
        .createQuery(), pageable);

    return getElideResponseBySearchResult(idAndSize, auth);
  }

  private Pair<List<Integer>, Integer> searchIdByLuceneQuery(
      Function<QueryBuilder, Query> function, Pageable pageable) {
    FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

    QueryBuilder queryBuilder = fullTextEntityManager
        .getSearchFactory()
        .buildQueryBuilder()
        .forEntity(Post.class)
        .get();

    org.hibernate.search.jpa.FullTextQuery hibernateQuery = fullTextEntityManager
        .createFullTextQuery(function.apply(queryBuilder), Post.class)
        .setTimeout(5L, TimeUnit.SECONDS)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .setProjection(ProjectionConstants.ID);

    List result = hibernateQuery.getResultList();
    int size = hibernateQuery.getResultSize();

    Pair<List<Integer>, Integer> resultIdList = Pair.of(toIntegerList(result), size);
    logger.debug("get {} lucene results: {}", size, resultIdList);

    return resultIdList;
  }

  private ElideResponse getElideResponseBySearchResult(
      Pair<List<Integer>, Integer> idAndSize, Principal auth) {
    List<Integer> ids = idAndSize.getLeft();
    if (ids.size() == 0) {
      return new ElideResponse(HttpStatus.OK.value(),
          "{\"data\":[],\"meta\":{\"page\":{\"totalRecords\":0}}}");
    }

    StringJoiner sj = new StringJoiner(",", "(", ")");
    ids.forEach(id -> sj.add(id.toString()));
    String inClause = sj.toString();

    Map<String, String> queryParams = ImmutableMap.<String, String>builder()
        .put("filter[post]", "id=in=" + inClause)
        .put("include", "topic,author")
        .put("fields[post]", "raw,cooked,topic,author,index,lastEditAt,createAt")
        .put("fields[topic]",
            "title,author,postIndex,categories,slug,locked,pinned,feature,createAt")
        .put("fields[user]", "username,nickname,email,emailPublic,avatar,bio,website,github,"
            + "topicCount,postCount,active,lockedAt,lockedUntil,lastSignInAt,createAt,group")
        .build();

    ElideResponse elideResponse = elide.get("/post", new MultivaluedHashMap<>(queryParams), auth);
    if (elideResponse.getResponseCode() == HttpStatus.OK.value()) {
      String body = elideResponse.getBody();
      String pageMeta = ",\"meta\":{\"page\":{\"totalRecords\":" + idAndSize.getRight() + "}}";

      return new ElideResponse(HttpStatus.OK.value(),
          new StringBuilder(body).insert(body.length() - 1, pageMeta).toString());
    } else {
      return elideResponse;
    }
  }

  @SuppressWarnings("unchecked")
  private List<Integer> toIntegerList(List list) {
    return (List<Integer>) list.stream()
        .map(item -> ((Object[]) item)[0])
        .collect(Collectors.toList());
  }

  @Async
  @Override
  public CompletableFuture<Boolean> safeReBuildIndex() {
    Boolean success = onlyOneReBuildSchedule(() -> {
      FullTextSession fullTextSession =
          org.hibernate.search.Search.getFullTextSession(entityManager.unwrap(Session.class));

      fullTextSession.setHibernateFlushMode(FlushMode.MANUAL);
      fullTextSession.setCacheMode(CacheMode.IGNORE);

      // TODO jpa 2.2 ?
      ScrollableResults results = fullTextSession.createCriteria(Post.class)
          .setFetchSize(BATCH_SIZE)
          .scroll(ScrollMode.FORWARD_ONLY);
      int index = 0;
      while (results.next()) {
        index++;
        fullTextSession.index(results.get(0)); //index each element
        if (index % BATCH_SIZE == 0) {
          fullTextSession.flushToIndexes(); //apply changes to indexes
          fullTextSession.clear(); //free memory since the queue is processed
        }
      }
    });
    return CompletableFuture.completedFuture(success);
  }

  @Async
  @Override
  public CompletableFuture<Boolean> reBuildIndex() {
    Boolean success = onlyOneReBuildSchedule(() -> {
      try {
        Search.getFullTextEntityManager(entityManager).createIndexer(Post.class).startAndWait();
      } catch (InterruptedException e) {
        logger.error("rebuild index throw InterruptedException", e);
      }
    });
    return CompletableFuture.completedFuture(success);
  }

  @PreAuthorize("hasAuthority('admin')")
  @Override
  public Boolean isIndexing() {
    return indexing.get();
  }

  /**
   * onlyOneReBuildSchedule.
   * @param schedule not create a thread
   */
  private Boolean onlyOneReBuildSchedule(Runnable schedule) {
    if (!indexing.compareAndSet(false, true)) {
      logger.warn("Index is rebuilding, ignore this request.");
      return false;
    }
    logger.info("Starting rebuild index.");
    schedule.run();
    logger.info("Rebuild index finished.");
    indexing.set(false);
    return true;
  }
}
