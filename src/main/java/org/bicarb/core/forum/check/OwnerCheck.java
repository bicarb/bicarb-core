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
import org.bicarb.core.forum.domain.Notification;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * OperationCheck, check entity owner whether current request user.
 *
 * @author olOwOlo
 */
public class OwnerCheck {

  public static final String USER_OWNER = "user.owner";
  public static final String NOTIFICATION_OWNER = "notification.owner";
  public static final String TOPIC_OWNER = "topic.owner";
  public static final String POST_OWNER = "post.owner";

  @ElideCheck(USER_OWNER)
  public static class UserOwner extends OperationCheck<User> {

    @Override
    public boolean ok(User user, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return AuthenticationUtils.getUserId(requestScope)
          .map(uid -> uid.equals(user.getId())).orElse(false);
    }
  }

  @ElideCheck(NOTIFICATION_OWNER)
  public static class NotificationOwner extends OperationCheck<Notification> {

    @Override
    public boolean ok(Notification notification, RequestScope requestScope,
        Optional<ChangeSpec> changeSpec) {
      return AuthenticationUtils.getUserId(requestScope)
          .map(uid -> uid.equals(notification.getTo().getId())).orElse(false);
    }
  }

  @ElideCheck(TOPIC_OWNER)
  public static class TopicOwner extends OperationCheck<Topic> {

    @Override
    public boolean ok(Topic topic, RequestScope requestScope,
        Optional<ChangeSpec> changeSpec) {
      return AuthenticationUtils.getUserId(requestScope)
          .map(uid -> uid.equals(topic.getAuthor().getId())).orElse(false);
    }
  }

  @ElideCheck(POST_OWNER)
  public static class PostOwner extends OperationCheck<Post> {

    @Override
    public boolean ok(Post post, RequestScope requestScope,
        Optional<ChangeSpec> changeSpec) {
      return AuthenticationUtils.getUserId(requestScope)
          .map(uid -> uid.equals(post.getAuthor().getId())).orElse(false);
    }
  }
}
