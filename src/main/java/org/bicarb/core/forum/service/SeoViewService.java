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

import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Setting;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.exception.ResourceNotFoundException;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.GroupRepository;
import org.bicarb.core.forum.repository.PostRepository;
import org.bicarb.core.forum.repository.SettingRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.Constant;
import org.bicarb.core.system.bean.SecureRandoms;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ViewService.
 *
 * @author olOwOlo
 */
@Service
@Transactional(readOnly = true)
public class SeoViewService {

  private final UserRepository userRepository;
  private final SettingRepository settingRepository;
  private final GroupRepository groupRepository;
  private final CategoryRepository categoryRepository;
  private final TopicRepository topicRepository;
  private final PostRepository postRepository;
  private final SecureRandoms secureRandoms;

  /**
   * Constructor.
   */
  @Autowired
  public SeoViewService(
      UserRepository userRepository,
      SettingRepository settingRepository,
      GroupRepository groupRepository,
      CategoryRepository categoryRepository,
      TopicRepository topicRepository,
      PostRepository postRepository,
      SecureRandoms secureRandoms) {
    this.userRepository = userRepository;
    this.settingRepository = settingRepository;
    this.groupRepository = groupRepository;
    this.categoryRepository = categoryRepository;
    this.topicRepository = topicRepository;
    this.postRepository = postRepository;
    this.secureRandoms = secureRandoms;
  }

  /**
   * topicListViewModel(topics).
   */
  public Map<String, Object> topicListViewModel(@Nullable Integer page, Principal auth) {
    page = page == null ? 1 : page;

    Page<Topic> topics = topicRepository
        .findAll(PageRequest.of(page - 1, 20, Sort.by(Order.desc("lastReplyAt"))));

    return ImmutableMap.<String, Object>builder()
        .putAll(getCommonModel(auth))
        .put("page", page)
        .put("topics", topics)
        .build();
  }

  /**
   * topicViewModel(topic).
   */
  public Map<String, Object> topicViewModel(
      Integer topicId, @Nullable Integer page, Principal auth) {

    Topic topic = topicRepository.findById(topicId).orElseThrow(ResourceNotFoundException::new);

    page = page == null ? 1 : page;

    Page<Post> posts = postRepository
        .findByTopic(topic, PageRequest.of(page - 1, 20, Sort.by("index")));

    return ImmutableMap.<String, Object>builder()
        .putAll(getCommonModel(auth))
        .put("page", page)
        .put("topic", topic)
        .put("posts", posts)
        .build();
  }

  /**
   * getCommonModel.
   */
  public Map<String, Object> getCommonModel(Principal auth) {
    User user = AuthenticationUtils.getUserId(auth)
        .map(userRepository::getOne)
        .orElse(null);

    Map<String, Object> common = ImmutableMap.<String, Object>builder()
        .put("settings", settingRepository.findAll().stream()
            .collect(Collectors.toMap(Setting::getKey, Setting::getValue)))
        .put("categories", categoryRepository.findAll())
        .put("groups", groupRepository.findAll())
        .build();

    if (user != null) {
      return ImmutableMap.<String, Object>builder()
          .putAll(common)
          .put("auth", user)
          .build();
    } else {
      return common;
    }
  }

  /**
   * Get nonce and Content-Security-Policy string.
   * If setting.{@link Constant#CSP} not exits, policy string will not be produced.
   * If policy string is not exist, should not add 'Content-Security-Policy' header.
   *
   * @return {@literal Pair<nonce, Optional<policy>>}
   */
  public Pair<String, Optional<String>> getNonceAndCsp() {
    String nonce = BaseEncoding.base64().encode(secureRandoms.generateBytes(16));
    return settingRepository.findById(Constant.CSP)
        .map(setting -> String.format(setting.getValue(), nonce))
        .map(policy -> Pair.of(nonce, Optional.of(policy)))
        .orElseGet(() -> Pair.of(nonce, Optional.empty()));
  }
}
