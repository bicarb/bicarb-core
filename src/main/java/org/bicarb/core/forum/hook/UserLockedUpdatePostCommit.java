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

import com.yahoo.elide.annotation.OnUpdatePostCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.security.BicarbSessionRegistry;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * UserLockedUpdatePostCommit, expire session to force user re-login.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnUpdatePostCommit.class, fieldOrMethodName = "lockedUntil")
public class UserLockedUpdatePostCommit implements LifeCycleHook<User> {

  private final BicarbSessionRegistry sessionRegistry;

  @Autowired
  public UserLockedUpdatePostCommit(BicarbSessionRegistry sessionRegistry) {
    this.sessionRegistry = sessionRegistry;
  }

  @Override
  public void execute(User user, RequestScope requestScope, Optional<ChangeSpec> optional) {
    sessionRegistry.expireSessionInformationByUsername(user.getUsername());
  }
}
