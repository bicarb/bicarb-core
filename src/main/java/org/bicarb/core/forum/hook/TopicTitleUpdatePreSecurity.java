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

import com.yahoo.elide.annotation.OnUpdatePreSecurity;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.bicarb.core.forum.domain.Topic;
import org.bicarb.core.system.bean.SlugifyHelper;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TopicTitleUpdatePreSecurity, Generate slug by title.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnUpdatePreSecurity.class, fieldOrMethodName = "title")
public class TopicTitleUpdatePreSecurity implements LifeCycleHook<Topic> {

  private final SlugifyHelper slugifyHelper;

  @Autowired
  public TopicTitleUpdatePreSecurity(SlugifyHelper slugifyHelper) {
    this.slugifyHelper = slugifyHelper;
  }

  @Override
  public void execute(Topic topic, RequestScope requestScope, Optional<ChangeSpec> changes) {
    topic.setSlug(slugifyHelper.slugify((String) changes.get().getModified()));
  }
}
