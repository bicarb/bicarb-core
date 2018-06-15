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

import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.core.ErrorObjects;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.exception.ConflictException;
import org.bicarb.core.forum.repository.GroupRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.validation.Validators;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * UserCreatePreSecurity.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreSecurity.class)
public class UserCreatePreSecurity implements LifeCycleHook<User> {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final Validators validators;

  /**
   * Constructor.
   */
  @Autowired
  public UserCreatePreSecurity(
      PasswordEncoder passwordEncoder,
      UserRepository userRepository,
      GroupRepository groupRepository,
      Validators validators) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.validators = validators;
  }

  @Override
  public void execute(User user, RequestScope requestScope, Optional<ChangeSpec> changes) {
    // register
    validators.validate(user);

    Optional<User> usernameUser = userRepository.findByUsernameIgnoreCase(user.getUsername());
    Optional<User> emailUser = userRepository.findByEmail(user.getEmail());
    Optional<User> nicknameUser = userRepository.findByNickname(user.getNickname());

    if (!usernameUser.isPresent() && !emailUser.isPresent() && !nicknameUser.isPresent()) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      // init default value
      user.setEmailPublic(false);
      user.setTopicCount(0);
      user.setPostCount(0);
      user.setActive(false);
      user.setCreateAt(Instant.now());
      user.setGroup(groupRepository.findById(3)
          .orElseThrow(() -> new IllegalStateException("Group 3 must exist and represent 'user'")));
    } else {
      List<Map<String, Object>> errors = new ArrayList<>();
      if (usernameUser.isPresent()) {
        errors.add(ImmutableMap.of("detail", "username conflict", "code", "4091"));
      }
      if (emailUser.isPresent()) {
        errors.add(ImmutableMap.of("detail", "email conflict", "code", "4092"));
      }
      if (nicknameUser.isPresent()) {
        errors.add(ImmutableMap.of("detail", "nickname conflict", "code", "4093"));
      }
      throw new ConflictException(new ErrorObjects(errors));
    }
  }
}
