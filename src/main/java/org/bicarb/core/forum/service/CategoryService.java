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

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.domain.Category;
import org.bicarb.core.forum.exception.InvalidObjectIdentifierException;
import org.bicarb.core.forum.exception.UnprocessableEntityException;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Category Service.
 *
 * @author olOwOlo
 */
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final TopicRepository topicRepository;

  @Autowired
  public CategoryService(CategoryRepository categoryRepository, TopicRepository topicRepository) {
    this.categoryRepository = categoryRepository;
    this.topicRepository = topicRepository;
  }

  /**
   * last + 1.
   */
  public Integer getNextPosition(Category parent) {
    Integer maxPosition;
    if (parent == null) {
      maxPosition = categoryRepository.findMaxPositionInRoot();
    } else {
      maxPosition = categoryRepository.findMaxPositionWithSameParent(parent);
      maxPosition = maxPosition == null ? -1 : maxPosition;
    }
    return maxPosition + 1;
  }

  /**
   * if parentId != null && position == null,
   * then position = {@link CategoryService#getNextPosition(Category)}.
   */
  @PreAuthorize("hasAuthority('" + PermissionCheck.CATEGORY_ALL + "')")
  @Transactional
  public void patchLocation(
      Integer categoryId,
      @Nullable Integer position,
      @Nullable Integer parentId) {

    if (position == null && parentId == null) {
      throw new UnprocessableEntityException(
          "position and parentId should not be null at the same time");
    }

    Category patched = categoryRepository.findById(categoryId).orElseThrow(() ->
        new InvalidObjectIdentifierException(categoryId.toString(), "categoryId"));

    Category newParent = null;

    // handle parentId
    if (parentId != null) {
      newParent = categoryRepository.findById(parentId).orElseThrow(() ->
          new InvalidObjectIdentifierException(parentId.toString(), "parentId"));

      // validate parentId
      Category current = newParent;
      while (current != null) {
        if (current.equals(patched)) {
          throw new UnprocessableEntityException(
              "new parent category cannot be its subcategory or itself");
        }
        current = current.getParent();
      }

      Category oldParent = patched.getParent();
      if (!newParent.equals(oldParent)) {
        Pair<Set<Category>, Set<Category>> addAndRemove = getChangePair(oldParent, newParent);

        addAndRemove.getLeft().forEach(category -> {
          category.setTopicCount(category.getTopicCount() + patched.getTopicCount());
          categoryRepository.addRelationsForCategory(patched.getId(), category.getId());
        });

        addAndRemove.getRight().forEach(category -> {
          category.setTopicCount(category.getTopicCount() - patched.getTopicCount());
          categoryRepository.removeRelationsForCategory(patched.getId(), category.getId());
        });

        topicRepository.invalidateAllTopic();
      }
    }

    // handle position
    if (position != null && !(parentId == null && position.equals(patched.getPosition()))) {
      categoryRepository
          .findByParentOrderByPositionDesc(parentId == null ? patched.getParent() : newParent)
          .stream()
          .filter(category -> category.getPosition() >= position)
          .forEachOrdered(category -> {
            category.setPosition(category.getPosition() + 1);
            categoryRepository.flush();
          });

      patched.setPosition(position);
    }

    // fallback to `getNextPosition`
    if (parentId != null && position == null) {
      patched.setPosition(getNextPosition(newParent));
    }

    // delay save avoiding violate unique constraint
    if (parentId != null) {
      patched.setParent(newParent);
    }
  }

  /**
   * Invoke {@link Category#getParent()}.
   */
  public Set<Category> getCategoriesIncludeParents(Category category) {
    Set<Category> cs = new HashSet<>();
    do {
      cs.add(category);
      category = category.getParent();
    } while (category != null);
    return cs;
  }

  /**
   * Get added and removed categories.
   */
  private Pair<Set<Category>, Set<Category>> getChangePair(Category oldParent, Category newParent) {
    Set<Category> oldParents = new HashSet<>();
    Category current = oldParent;
    while (current != null) {
      oldParents.add(current);
      current = current.getParent();
    }

    Set<Category> newParents = new HashSet<>();
    current = newParent;
    while (current != null) {
      newParents.add(current);
      current = current.getParent();
    }

    Set<Category> addParents = new HashSet<>(newParents);
    addParents.removeAll(oldParents);
    Set<Category> removeParents = new HashSet<>(oldParents);
    removeParents.removeAll(newParents);

    return Pair.of(addParents, removeParents);
  }
}
