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
import org.bicarb.core.forum.domain.Notification;
import org.bicarb.core.forum.domain.Notification.NotificationType;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.NotificationRepository;
import org.bicarb.core.forum.service.PostService;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * PostCreatePreCommit.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreCommit.class)
public class PostCreatePreCommit implements LifeCycleHook<Post> {

  private static final Logger logger = LoggerFactory.getLogger(PostCreatePreCommit.class);

  private final PostService postService;
  private final NotificationRepository notificationRepository;

  @Autowired
  public PostCreatePreCommit(
      PostService postService,
      NotificationRepository notificationRepository) {
    this.postService = postService;
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void execute(Post post, RequestScope requestScope, Optional<ChangeSpec> changes) {
    postService.handleCreateMention(post);

    User author = post.getAuthor();
    Topic topic = post.getTopic();

    // other reply
    if (!topic.getAuthor().equals(author)) {
      Notification notification = Notification.builder()
          .type(NotificationType.REPLY)
          .send(post.getAuthor())
          .to(topic.getAuthor())
          .post(post)
          .topic(post.getTopic())
          .createAt(Instant.now())
          .build();
      notificationRepository.save(notification);
      logger.info("user [{}] mention [{}] in post [{}]",
          author.getUsername(), topic.getAuthor().getUsername(), post.getId());
    }

    // TODO handle reply to

    // set count
    author.setPostCount(author.getPostCount() + 1);
    topic.setPostIndex(post.getIndex());
    // set last reply
    topic.setLastReplyBy(author);
    topic.setLastReplyAt(Instant.now());
  }
}
