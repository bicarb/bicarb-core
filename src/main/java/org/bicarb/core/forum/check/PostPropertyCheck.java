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
import org.bicarb.core.forum.domain.Post;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * Post Property Check.
 *
 * @author olOwOlo
 */
public class PostPropertyCheck {

  public static final String NON_DELETE = "post.delete.false";
  public static final String NON_DELETE_BY_OTHER = "post.delete.by.other.false";
  public static final String TOPIC_NON_DELETE = "post.topic.delete.false";
  public static final String TOPIC_NON_LOCKED = "post.topic.locked.false";

  @ElideCheck(NON_DELETE)
  public static class NonDeleteCheck extends OperationCheck<Post> {

    @Override
    public boolean ok(Post post, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return !post.getDelete();
    }
  }

  @ElideCheck(NON_DELETE_BY_OTHER)
  public static class NonDeleteByOtherCheck extends OperationCheck<Post> {

    @Override
    public boolean ok(Post post, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return post.getDeleteBy() == null || post.getAuthor().equals(post.getDeleteBy());
    }
  }

  @ElideCheck(TOPIC_NON_DELETE)
  public static class TopicNonDelete extends OperationCheck<Post> {

    @Override
    public boolean ok(Post post, RequestScope requestScope, Optional<ChangeSpec> optional) {
      return !post.getTopic().getDelete();
    }
  }

  @ElideCheck(TOPIC_NON_LOCKED)
  public static class TopicNonLocked extends OperationCheck<Post> {

    @Override
    public boolean ok(Post post, RequestScope requestScope, Optional<ChangeSpec> optional) {
      return !post.getTopic().getLocked();
    }
  }
}
