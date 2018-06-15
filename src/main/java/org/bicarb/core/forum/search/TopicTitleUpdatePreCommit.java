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

import com.yahoo.elide.annotation.OnUpdatePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.repository.PostRepository;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manual update index when topic.title is updated.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnUpdatePreCommit.class, fieldOrMethodName = "title")
public class TopicTitleUpdatePreCommit implements LifeCycleHook<Topic> {

  private final EntityManager entityManager;
  private final PostRepository postRepository;

  @Autowired
  public TopicTitleUpdatePreCommit(EntityManager entityManager, PostRepository postRepository) {
    this.entityManager = entityManager;
    this.postRepository = postRepository;
  }

  @Override
  public void execute(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changes) {
    FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
    postRepository.findByTopic(topic).forEach(fullTextEntityManager::index);
  }
}
