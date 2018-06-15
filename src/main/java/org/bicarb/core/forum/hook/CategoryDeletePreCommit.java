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

import com.yahoo.elide.annotation.OnDeletePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.bicarb.core.forum.domain.Category;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CategoryDeletePreCommit.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnDeletePreCommit.class)
public class CategoryDeletePreCommit implements LifeCycleHook<Category> {

  private final CategoryRepository categoryRepository;
  private final TopicRepository topicRepository;

  @Autowired
  public CategoryDeletePreCommit(
      CategoryRepository categoryRepository,
      TopicRepository topicRepository) {
    this.categoryRepository = categoryRepository;
    this.topicRepository = topicRepository;
  }

  @Override
  public void execute(Category category, RequestScope requestScope, Optional<ChangeSpec> optional) {
    // manual delete relations
    categoryRepository.removeRelationsByCategoryId(category.getId());
    topicRepository.invalidateAllTopic();
  }
}
