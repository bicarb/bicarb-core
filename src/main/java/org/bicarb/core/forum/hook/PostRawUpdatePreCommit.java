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

import com.yahoo.elide.annotation.OnUpdatePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.time.Instant;
import java.util.Optional;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.service.PostService;
import org.bicarb.core.system.bean.Renderer;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Post Raw UpdatePreCommit.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnUpdatePreCommit.class, fieldOrMethodName = "raw")
public class PostRawUpdatePreCommit implements LifeCycleHook<Post> {

  private final Renderer renderer;
  private final PostService postService;

  @Autowired
  public PostRawUpdatePreCommit(Renderer renderer, PostService postService) {
    this.renderer = renderer;
    this.postService = postService;
  }

  @Override
  public void execute(Post post, RequestScope requestScope, Optional<ChangeSpec> changes) {
    if (post.getIndex() == 0) {
      post.setCooked(renderer.renderTopic(post.getRaw()));
    } else {
      post.setCooked(renderer.renderPost(post.getRaw()));
    }
    post.setLastEditAt(Instant.now());
    postService.handleUpdateMention(post, ((String) changes.get().getOriginal()));
  }
}
