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

package org.bicarb.core.forum.hook;

import com.yahoo.elide.annotation.OnCreatePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.time.Instant;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.PostRepository;
import org.bicarb.core.forum.service.PostService;
import org.bicarb.core.system.bean.Renderer;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TopicCreatePreCommit.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreCommit.class)
public class TopicCreatePreCommit implements LifeCycleHook<Topic> {

  private final PostService postService;
  private final PostRepository postRepository;
  private final Renderer renderer;
  private final HttpServletRequest request;

  /** Constructor. */
  @Autowired
  public TopicCreatePreCommit(
      PostService postService,
      PostRepository postRepository,
      Renderer renderer,
      HttpServletRequest request) {
    this.postService = postService;
    this.postRepository = postRepository;
    this.renderer = renderer;
    this.request = request;
  }

  @Override
  public void execute(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changes) {
    // generate body
    String cooked = renderer.renderTopic(topic.getBody());

    Post post = Post.builder()
        .raw(topic.getBody())
        .cooked(cooked)
        .topic(topic)
        .author(topic.getAuthor())
        .index(0)
        .ip(request.getRemoteAddr())
        // default value
        .delete(false)
        .createAt(Instant.now())
        .build();

    postRepository.save(post);

    postService.handleCreateMention(post);

    // handle count
    User author = topic.getAuthor();
    author.setTopicCount(author.getTopicCount() + 1);
    topic.getCategories().forEach(category -> category.setTopicCount(category.getTopicCount() + 1));
  }
}
