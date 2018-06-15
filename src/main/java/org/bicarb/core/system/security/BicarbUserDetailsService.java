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

import java.util.stream.Collectors;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BicarbUserDetailsService.
 *
 * @author olOwOlo
 */
@Service
public class BicarbUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(BicarbUserDetailsService.class);

  private final UserRepository userRepository;

  @Autowired
  public BicarbUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  @Override
  public UserDetails loadUserByUsername(String signIn) throws UsernameNotFoundException {

    User user = userRepository.findByUsernameIgnoreCase(signIn)
        .orElseGet(() -> userRepository.findByEmail(signIn)
            .orElseThrow(() -> new UsernameNotFoundException("username or email not found.")));

    logger.debug("load user by signIn[{}]: {}", signIn, user);

    return new BicarbUserDetails(
        user.getId(),
        user.getUsername(),
        user.getPassword(),
        user.getActive(),
        user.getLockedUntil(),
        user.getGroup().getPermissions()
            .stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
  }

}
