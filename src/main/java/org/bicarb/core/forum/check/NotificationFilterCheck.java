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

import com.yahoo.elide.core.Path;
import com.yahoo.elide.core.Path.PathElement;
import com.yahoo.elide.core.filter.FilterPredicate;
import com.yahoo.elide.core.filter.Operator;
import com.yahoo.elide.core.filter.expression.FilterExpression;
import com.yahoo.elide.security.FilterExpressionCheck;
import com.yahoo.elide.security.RequestScope;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.bicarb.core.forum.domain.Notification;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * NotificationFilterCheck.
 *
 * @author olOwOlo
 */
public class NotificationFilterCheck {

  public static final String OWNER = "notification.owner.filter";

  @ElideCheck(OWNER)
  public static class NotificationOwnerFilter extends FilterExpressionCheck<Notification> {

    @Override
    public FilterExpression getFilterExpression(Class<?> entityClass, RequestScope requestScope) {
      Optional<Integer> uid = AuthenticationUtils.getUserId(requestScope);
      if (uid.isPresent()) {

        PathElement notificationPath = new PathElement(Notification.class, User.class, "to");
        PathElement userPath = new PathElement(User.class, Integer.class, "id");
        Path path = new Path(Arrays.asList(notificationPath, userPath));

        return new FilterPredicate(path, Operator.IN, Collections.singletonList(uid.get()));
      } else {
        // filter all
        return new FilterPredicate(
            new PathElement(Notification.class, Integer.class, "id"),
            Operator.ISNULL,
            Collections.emptyList());
      }
    }
  }
}
