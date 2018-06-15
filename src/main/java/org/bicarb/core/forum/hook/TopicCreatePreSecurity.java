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
import com.yahoo.elide.core.exceptions.InvalidConstraintException;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.time.Instant;
import java.util.Optional;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.forum.service.CategoryService;
import org.bicarb.core.system.bean.SlugifyHelper;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.bicarb.core.system.validation.Validators;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Topic CreatePreSecurity.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreSecurity.class)
public class TopicCreatePreSecurity implements LifeCycleHook<Topic> {

  private final SlugifyHelper slugifyHelper;
  private final Validators validators;
  private final UserRepository userRepository;
  private final CategoryService categoryService;

  /** Constructor. */
  @Autowired
  public TopicCreatePreSecurity(
      SlugifyHelper slugifyHelper,
      Validators validators,
      UserRepository userRepository,
      CategoryService categoryService) {
    this.slugifyHelper = slugifyHelper;
    this.validators = validators;
    this.userRepository = userRepository;
    this.categoryService = categoryService;
  }

  @Override
  public void execute(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changes) {
    validators.validate(topic);

    if (topic.getBody() == null) {
      throw new InvalidConstraintException("body should not be null");
    }

    // get author
    User author = userRepository.getOne(AuthenticationUtils.fetchUserId(requestScope));
    topic.setAuthor(author);

    // get all parent Category and set
    if (topic.getCategories().size() != 1) {
      throw new InvalidConstraintException("categories should have only one category, "
          + "its parents will be added automatically.");
    }
    topic.setCategories(categoryService
        .getCategoriesIncludeParents(topic.getCategories().iterator().next()));

    // slug
    topic.setSlug(slugifyHelper.slugify(topic.getTitle()));

    // init default value
    topic.setPostIndex(0);
    topic.setLocked(false);
    topic.setDelete(false);
    topic.setPinned(false);
    topic.setFeature(false);
    Instant now = Instant.now();
    topic.setCreateAt(now);
    topic.setLastReplyAt(now);
  }
}
