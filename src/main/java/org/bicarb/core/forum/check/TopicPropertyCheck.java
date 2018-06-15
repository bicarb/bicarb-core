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
import org.bicarb.core.forum.domain.Topic;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * Topic Property Check.
 *
 * @author olOwOlo
 */
public class TopicPropertyCheck {

  public static final String NON_DELETE_BY_OTHER = "topic.delete.by.other.false";
  public static final String NON_LOCKED_BY_OTHER = "topic.locked.by.other.false";

  @ElideCheck(NON_DELETE_BY_OTHER)
  public static class NonDeleteByOtherCheck extends OperationCheck<Topic> {

    @Override
    public boolean ok(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return topic.getDeleteBy() == null || topic.getAuthor().equals(topic.getDeleteBy());
    }
  }

  @ElideCheck(NON_LOCKED_BY_OTHER)
  public static class NonLockedByOtherCheck extends OperationCheck<Topic> {

    @Override
    public boolean ok(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return topic.getLockedBy() == null || topic.getAuthor().equals(topic.getLockedBy());
    }
  }
}
