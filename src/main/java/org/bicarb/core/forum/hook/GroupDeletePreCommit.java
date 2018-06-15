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

import com.google.common.collect.ImmutableSet;
import com.yahoo.elide.annotation.OnDeletePreCommit;
import com.yahoo.elide.core.exceptions.ForbiddenAccessException;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import java.util.Set;
import org.bicarb.core.forum.domain.Group;
import org.bicarb.core.forum.repository.GroupRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * GroupDeletePreCommit.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnDeletePreCommit.class)
public class GroupDeletePreCommit implements LifeCycleHook<Group> {

  private final Set<Integer> preDefine = ImmutableSet.of(1, 2, 3);

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  @Autowired
  public GroupDeletePreCommit(UserRepository userRepository, GroupRepository groupRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  @Override
  public void execute(Group group, RequestScope requestScope, Optional<ChangeSpec> changes) {
    if (preDefine.contains(group.getId())) {
      throw new ForbiddenAccessException("delete predefine group is not allowed");
    }

    userRepository.updateUserGroup(group, groupRepository.findById(3)
        .orElseThrow(() -> new IllegalStateException("Group 3 must exist and represent 'user'")));
  }
}
