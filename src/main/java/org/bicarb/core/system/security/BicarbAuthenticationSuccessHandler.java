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

package org.bicarb.core.system.security;

import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * No redirect.
 *
 * @author olOwOlo
 */
@Component
public class BicarbAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserRepository userRepository;

  @Autowired
  public BicarbAuthenticationSuccessHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication) {
    clearAuthenticationAttributes(request);

    // last sign in ip and time
    Integer userId = ((BicarbUserDetails) authentication.getPrincipal()).getId();
    User user = userRepository.findById(userId).orElseThrow(IllegalStateException::new);
    user.setLastSignInAt(Instant.now());
    user.setLastSignIp(IpUtils.getClientIp(request));
    userRepository.save(user);
  }
}
