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

import com.yahoo.elide.core.ErrorObjects;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.domain.Category;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.bicarb.core.system.security.BicarbUserDetails;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TopicService.
 *
 * @author olOwOlo
 */
@Transactional
@Service
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;
  private final CategoryService categoryService;

  /** Constructor. */
  @Autowired
  public TopicService(
      TopicRepository topicRepository,
      CategoryRepository categoryRepository,
      CategoryService categoryService) {
    this.topicRepository = topicRepository;
    this.categoryRepository = categoryRepository;
    this.categoryService = categoryService;
  }

  /**
   * Update topic categories.
   * @param topicId topic id
   * @param categoryId category id, all parents will be added automatically
   * @param authentication current principal
   * @return {@literal Optional<Pair<Error Status, Error Objects>>}
   */
  @PreAuthorize("hasAnyAuthority('" + PermissionCheck.TOPIC_EDIT_CATEGORY + "', "
      + "'" + PermissionCheck.TOPIC_EDIT_OWN_CATEGORY + "')")
  public Optional<Pair<Integer, ErrorObjects>> patchCategories(
      Integer topicId, Integer categoryId, Principal authentication) {

    Optional<Topic> topic = topicRepository.findById(topicId);
    Optional<Category> category = categoryRepository.findById(categoryId);

    if (!topic.isPresent()) {
      return Optional.of(Pair.of(HttpStatus.NOT_FOUND.value(), ErrorObjects.builder()
          .addError().withDetail("Unknown identifier '" + topicId + "' for topic").build()));
    }

    if (!category.isPresent()) {
      return Optional.of(Pair.of(HttpStatus.NOT_FOUND.value(), ErrorObjects.builder()
          .addError().withDetail("Unknown identifier '" + categoryId + "' for topic").build()));
    }

    BicarbUserDetails userDetails = AuthenticationUtils.fetchUserDetails(authentication);
    if (!userDetails.hasPermission(PermissionCheck.TOPIC_EDIT_CATEGORY)
        && !userDetails.getId().equals(topic.get().getAuthor().getId())) {
      return Optional.of(Pair.of(HttpStatus.FORBIDDEN.value(), ErrorObjects.builder()
          .addError().withDetail("Access Forbidden").build()));
    }

    // end valid check

    Set<Category> newCategories = categoryService.getCategoriesIncludeParents(category.get());
    Set<Category> oldCategories = topic.get().getCategories();

    newCategories.forEach(c -> c.setTopicCount(c.getTopicCount() + 1));
    oldCategories.forEach(c -> c.setTopicCount(c.getTopicCount() - 1));

    topic.get().setCategories(newCategories);

    return Optional.empty();
  }
}
