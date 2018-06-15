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

package org.bicarb.core.forum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.PostRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.bicarb.core.forum.search.SearchService;
import org.hamcrest.number.OrderingComparison;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * TODO This should be refactored when junit 5 support order.
 * https://github.com/junit-team/junit5/issues/48
 *
 * @author olOwOlo
 */
@WithUserDetails("admin")
public class SearchTest extends BaseSetup {

  @Autowired
  private SearchService searchService;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private TopicRepository topicRepository;
  @Autowired
  private PlatformTransactionManager txManager;

  @Test
  void test() throws Exception {
    try {
      commitTx(() -> searchService.reBuildIndex().join());

      // test create topic and post
      commitTx(() -> mockRequest.post(mockMvc, "/api/topic", jsonBody.getJson("/search/postTopic.json"))
          .andExpect(status().isCreated()));

      commitTx(() -> mockMvc.perform(get("/api/search?q=新想法"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(1)));

      Integer innerTid = topicRepository.findAll(Sort.by(Order.desc("id"))).get(0).getId();

      // test update topic title
      commitTx(() -> mockRequest.patch(mockMvc, "/api/topic/" + innerTid, "{ \"data\": { \"type\": \"topic\", \"id\": \"" + innerTid +"\", \"attributes\": { \"title\": \"Nothing\" } } }")
          .andExpect(status().isNoContent()));

      commitTx(() -> mockMvc.perform(get("/api/search?q=新想法"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(0)));

      // test delete(soft) topic
      commitTx(() -> mockRequest.patch(mockMvc, "/api/topic/" + innerTid, "{ \"data\": { \"type\": \"topic\", \"id\": \"" + innerTid +"\", \"attributes\": { \"delete\": \"true\" } } }")
          .andExpect(status().isNoContent()));

      commitTx(() -> mockMvc.perform(get("/api/search?q=回家结婚"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(0)));

      // test restore(soft) topic
      commitTx(() -> mockRequest.patch(mockMvc, "/api/topic/" + innerTid, "{ \"data\": { \"type\": \"topic\", \"id\": \"" + innerTid +"\", \"attributes\": { \"delete\": \"false\" } } }")
          .andExpect(status().isNoContent()));

      commitTx(() -> mockMvc.perform(get("/api/search?q=回家结婚"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(1)));
    } finally {
      // restore all changes(created a topic)
      commitTx(() -> topicRepository.findAll().stream()
          .filter(topic -> topic.getId() > 1)
          .forEach(topic -> {
            // user topic count
            User author = topic.getAuthor();
            author.setTopicCount(author.getTopicCount() - 1);
            // category count
            topic.getCategories().forEach(category ->
                category.setTopicCount(category.getTopicCount() - 1));
            // delete posts and topic
            postRepository.findByTopic(topic).forEach(postRepository::delete);
            topicRepository.delete(topic);
          })
      );

      commitTx(() -> searchService.safeReBuildIndex().join());
    }
  }

  @Test
  void testSearchRelate() throws Exception {
    mockMvc.perform(get("/api/search/1/relate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.meta.page.totalRecords").value(4))
        .andExpect(jsonPath("$.included.length()").value(OrderingComparison.greaterThan(0)));
  }

  @Test
  void testOnlyOneReBuildSchedule() {
    List<Boolean> results = new ArrayList<>();

    CompletableFuture.allOf(
        searchService.safeReBuildIndex().thenAccept(results::add),
        searchService.reBuildIndex().thenAccept(results::add)
    ).join();

    assertThat(results.stream()
        .filter(Boolean.TRUE::equals)
        .collect(Collectors.toList())
        .size()
    ).isEqualTo(1);
  }

  @WithUserDetails
  @Test
  void testNoPermission() throws Exception {
    mockRequest.postForm(mockMvc, "/api/index/rebuild", "safe=true")
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/index/building"))
        .andExpect(status().isForbidden());
  }

  private void commitTx(Run run) throws Exception {
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("new tx");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus status = txManager.getTransaction(def);
    try {
      run.run();
    } catch (Exception ex) {
      txManager.rollback(status);
      throw ex;
    }
    txManager.commit(status);
  }

  private interface Run {
    void run() throws Exception;
  }
}
