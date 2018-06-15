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

package org.bicarb.core.forum.check;

import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.OperationCheck;
import java.util.Optional;
import org.bicarb.core.forum.domain.User;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * UserPropertyCheck.
 *
 * @author olOwOlo
 */
public class UserPropertyCheck {

  public static final String EMAIL_PUBLIC = "email.public";

  @ElideCheck(EMAIL_PUBLIC)
  public static class EmailCheck extends OperationCheck<User> {

    @Override
    public boolean ok(User user, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return user.getEmailPublic();
    }
  }
}
