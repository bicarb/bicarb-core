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
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * PostFilterCheck.
 *
 * @author olOwOlo
 */
public class PostFilterCheck {

  public static final String NON_DELETE = "post.delete.false.filter";
  public static final String TOPIC_NON_DELETE = "post.topic.delete.false.filter";
  // A post can only be deleted by the person who has the `post.delete` permission,
  // or the post owner who has `post.delete.own` permission,
  // the former has permission to read all post, so just check whether
  // this post is deleted by current user is enough.
  public static final String DELETE_BY_SELF = "post.delete.by.self.filter";

  @ElideCheck(NON_DELETE)
  public static class PostNotDeleteFilter extends FilterExpressionCheck<Post> {

    @Override
    public FilterExpression getFilterExpression(Class<?> entityClass, RequestScope requestScope) {

      return new FilterPredicate(
          new PathElement(Post.class, Boolean.class, "delete"),
          Operator.IN,
          Collections.singletonList(false));
    }
  }

  @ElideCheck(TOPIC_NON_DELETE)
  public static class PostTopicNotDeleteFilter extends FilterExpressionCheck<Post> {

    @Override
    public FilterExpression getFilterExpression(Class<?> entityClass, RequestScope requestScope) {

      PathElement postPath = new PathElement(Post.class, Topic.class, "topic");
      PathElement topicPath = new PathElement(Topic.class, Boolean.class, "delete");
      Path path = new Path(Arrays.asList(postPath, topicPath));

      return new FilterPredicate(path, Operator.IN, Collections.singletonList(false));
    }
  }

  @ElideCheck(DELETE_BY_SELF)
  public static class PostNonDeleteByOtherFilter extends FilterExpressionCheck<Post> {

    @Override
    public FilterExpression getFilterExpression(Class<?> entityClass, RequestScope requestScope) {
      return getPropertyUserIdInCurrentUserIdPredicate("deleteBy", requestScope);
    }
  }

  /**
   * getPropertyUserIdInCurrentUserIdPredicate.
   *
   * @param propertyName propertyName
   * @param requestScope requestScope
   * @return If uid(current user id) exist, return `((User) propertyName).id in (uid)`,
   *     otherwise return topic.id not null.
   */
  private static FilterPredicate getPropertyUserIdInCurrentUserIdPredicate(
      String propertyName, RequestScope requestScope) {
    Optional<Integer> uid = AuthenticationUtils.getUserId(requestScope);
    if (uid.isPresent()) {
      PathElement postPath = new PathElement(Post.class, User.class, propertyName);
      PathElement userPath = new PathElement(User.class, Integer.class, "id");
      Path path = new Path(Arrays.asList(postPath, userPath));

      return new FilterPredicate(path, Operator.IN, Collections.singletonList(uid.get()));
    } else {
      // return no effect predicate.
      return new FilterPredicate(
          new PathElement(Post.class, Integer.class, "id"),
          Operator.NOTNULL,
          Collections.emptyList());
    }
  }
}
