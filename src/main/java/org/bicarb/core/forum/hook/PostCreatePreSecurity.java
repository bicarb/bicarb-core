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

import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.time.Instant;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.bean.Renderer;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.bicarb.core.system.validation.Validators;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * PostCreatePreSecurity.
 *
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreSecurity.class)
public class PostCreatePreSecurity implements LifeCycleHook<Post> {

  private static final Logger logger = LoggerFactory.getLogger(PostCreatePreSecurity.class);

  private final Validators validators;
  private final UserRepository userRepository;
  private final HttpServletRequest request;
  private final Renderer renderer;

  /** Constructor. */
  @Autowired
  public PostCreatePreSecurity(
      Validators validators,
      UserRepository userRepository,
      HttpServletRequest request,
      Renderer renderer) {
    this.validators = validators;
    this.userRepository = userRepository;
    this.request = request;
    this.renderer = renderer;
  }

  @Override
  public void execute(Post post, RequestScope requestScope, Optional<ChangeSpec> changes) {
    validators.validate(post);

    // get author
    User author = userRepository.getOne(AuthenticationUtils.fetchUserId(requestScope));
    post.setAuthor(author);

    // set index
    post.setIndex(post.getTopic().getPostIndex() + 1);

    // cook
    post.setCooked(renderer.renderPost(post.getRaw()));

    // ip
    post.setIp(request.getRemoteAddr());

    // init default value
    post.setDelete(false);
    post.setCreateAt(Instant.now());

    logger.debug("prepare post: {}", post);
  }
}
