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

package org.bicarb.core.forum.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.bicarb.core.forum.domain.Notification;
import org.bicarb.core.forum.domain.Notification.NotificationType;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.NotificationRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.config.BicarbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostService.
 *
 * @author olOwOlo
 */
@Service
public class PostService {

  private static final Logger logger = LoggerFactory.getLogger(PostService.class);

  private static final Pattern rawPattern = Pattern.compile("@(\\w{1,30}) ");

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final String userLinkFormat;

  /** Constructor. */
  @Autowired
  public PostService(UserRepository userRepository,
      NotificationRepository notificationRepository,
      BicarbProperties bicarbProperties) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
    this.userLinkFormat = bicarbProperties.getUserLinkFormat();
  }

  /**
   * Side effect: update cooked.
   * @param post post have been cooked, managed by jpa
   * @param oldRaw old raw content
   * @see #getMentionedList(Post, String)
   * @see #sendNotification(List, Post)
   */
  public void handleUpdateMention(Post post, @Nullable String oldRaw) {
    sendNotification(getMentionedList(post, oldRaw), post);
  }

  /**
   * Side effect: update cooked.
   * @see PostService#handleUpdateMention(Post, String)
   */
  public void handleCreateMention(Post post) {
    handleUpdateMention(post, null);
  }

  /**
   * Side effect: update cooked.
   * @param post post has cooked field
   * @param oldRaw old raw content
   * @return users who should be notified
   */
  @Transactional(readOnly = true)
  public List<User> getMentionedList(Post post, @Nullable String oldRaw) {
    // superset
    Set<String> existUsername = new HashSet<>();
    if (oldRaw != null) {
      Matcher matcher = rawPattern.matcher(oldRaw);
      while (matcher.find()) {
        existUsername.add(matcher.group(1));
      }
    }
    List<User> mentionedList = new ArrayList<>();

    Matcher matcher = rawPattern.matcher(post.getCooked());
    String s = matcher.replaceAll(result -> {
      String raw = result.group();
      String username = raw.substring(1, raw.length() - 1);

      if (existUsername.contains(username)) {
        return String.format(userLinkFormat, username, username);
      }

      Optional<User> up = userRepository.findByUsernameIgnoreCase(username);
      up.ifPresent(mentionedList::add);

      return up.isPresent()
          ? String.format(userLinkFormat, username, username)
          : raw;
    });
    post.setCooked(s);
    return mentionedList;
  }

  /**
   * sendNotification.
   * @param users users who should be notified
   * @param post post managed by jpa
   */
  private void sendNotification(List<User> users, Post post) {
    for (User u: users) {
      Notification notification = Notification.builder()
          .type(NotificationType.MENTION)
          .send(post.getAuthor())
          .to(u)
          .post(post)
          .topic(post.getTopic())
          .createAt(Instant.now())
          .build();
      notificationRepository.save(notification);
      logger.info("user [{}] mention [{}] in post [{}]",
          post.getAuthor().getUsername(), u.getUsername(), post.getId());

      // TODO webSocket
    }
  }
}
