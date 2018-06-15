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

import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.core.ErrorObjects;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.bicarb.core.forum.domain.Category;
import org.bicarb.core.forum.exception.ConflictException;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.service.CategoryService;
import org.bicarb.core.system.bean.SlugifyHelper;
import org.bicarb.core.system.validation.Validators;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CategoryCreatePreSecurity.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreSecurity.class)
public class CategoryCreatePreSecurity implements LifeCycleHook<Category> {

  private final Validators validators;
  private final SlugifyHelper slugifyHelper;
  private final CategoryRepository categoryRepository;
  private final CategoryService categoryService;

  /** Constructor. */
  @Autowired
  public CategoryCreatePreSecurity(
      Validators validators,
      SlugifyHelper slugifyHelper,
      CategoryRepository categoryRepository,
      CategoryService categoryService) {
    this.validators = validators;
    this.slugifyHelper = slugifyHelper;
    this.categoryRepository = categoryRepository;
    this.categoryService = categoryService;
  }

  @Override
  public void execute(Category category, RequestScope requestScope, Optional<ChangeSpec> changes) {
    validators.validate(category);

    // set slug
    if (category.getSlug() == null) {
      category.setSlug(slugifyHelper.slugify(category.getName()));
    }

    if (categoryRepository.existsBySlug(category.getSlug())) {
      throw new ConflictException(ErrorObjects.builder()
          .addError()
          .withCode("4094")
          .withDetail("slug[" + category.getSlug() + "] is already exists")
          .build());
    }

    // set position, last
    category.setPosition(categoryService.getNextPosition(category.getParent()));

    // init default value
    category.setTopicCount(0);
  }
}
