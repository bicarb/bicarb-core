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

import com.yahoo.elide.annotation.OnDeletePreSecurity;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.bicarb.core.forum.domain.Category;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.service.CategoryService;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CategoryDeletePreSecurity, change parent and position for sub category.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnDeletePreSecurity.class)
public class CategoryDeletePreSecurity implements LifeCycleHook<Category> {

  private static final Logger logger = LoggerFactory.getLogger(CategoryDeletePreSecurity.class);

  private final CategoryRepository categoryRepository;
  private final CategoryService categoryService;

  @Autowired
  public CategoryDeletePreSecurity(
      CategoryRepository categoryRepository,
      CategoryService categoryService) {
    this.categoryRepository = categoryRepository;
    this.categoryService = categoryService;
  }

  @Override
  public void execute(Category category, RequestScope requestScope, Optional<ChangeSpec> changes) {
    logger.debug("before delete category[{}]", category);
    // set new parent and position.
    categoryRepository.findByParentOrderByPositionDesc(category).forEach(c -> {
      logger.debug("change parent and position for {}", c);
      c.setPosition(categoryService.getNextPosition(category.getParent()));
      c.setParent(category.getParent());
    });
  }
}
