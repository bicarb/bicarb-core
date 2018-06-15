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
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * BicarbUserDetails.
 *
 * @author olOwOlo
 */
public class BicarbUserDetails extends org.springframework.security.core.userdetails.User {

  private static final long serialVersionUID = 1L;

  @Getter
  private final Integer id;
  @Getter
  private final Boolean active;
  @Getter
  private final Instant lockedUntil;

  /** Constructor.*/
  public BicarbUserDetails(
      Integer id,
      String username,
      String password,
      Boolean active,
      Instant lockedUntil,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.id = id;
    this.active = active;
    this.lockedUntil = lockedUntil;
  }

  public boolean hasPermission(String permission) {
    return getAuthorities().contains(new SimpleGrantedAuthority(permission));
  }
}
