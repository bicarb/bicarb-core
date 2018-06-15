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

package org.bicarb.core.system.util;

import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.User;
import java.security.Principal;
import java.util.Optional;
import org.bicarb.core.system.security.BicarbUserDetails;
import org.springframework.security.core.Authentication;

/**
 * <p>
 *   Authentication Utils.
 * </p>
 * <p>
 *   <code>Principal</code> is obtained from <code>Controller</code> method argument.
 * </p>
 * <p>
 *   Unlike <code>SecurityContextHolder.getContext().getAuthentication().getPrincipal()</code>,
 *   it seems to return <code>null</code> instead of anonymous user.
 * </p>
 *
 * @author olOwOlo
 */
public class AuthenticationUtils {

  /** getUserDetails. */
  public static Optional<BicarbUserDetails> getUserDetails(User user) {
    Authentication authentication = (Authentication) user.getOpaqueUser();
    if (authentication == null) {
      return Optional.empty();
    } else {
      return Optional.of(((BicarbUserDetails) authentication.getPrincipal()));
    }
  }

  /** getUserDetails. */
  public static Optional<BicarbUserDetails> getUserDetails(Principal authentication) {
    if (authentication == null) {
      return Optional.empty();
    } else {
      return Optional.of((BicarbUserDetails) ((Authentication) authentication).getPrincipal());
    }
  }

  public static BicarbUserDetails fetchUserDetails(Principal authentication) {
    return getUserDetails(authentication).orElseThrow(() ->
        new IllegalStateException("This method should be used for authenticated request"));
  }

  public static Optional<Integer> getUserId(RequestScope requestScope) {
    return getUserDetails(requestScope.getUser()).map(BicarbUserDetails::getId);
  }

  /** getUserId. */
  public static Optional<Integer> getUserId(Principal authentication) {
    if (authentication == null) {
      return Optional.empty();
    } else {
      Object auth = ((Authentication) authentication).getPrincipal();
      return Optional.of(((BicarbUserDetails) auth).getId());
    }
  }

  public static Integer fetchUserId(RequestScope requestScope) {
    return getUserId(requestScope).orElseThrow(() ->
        new IllegalStateException("This method should be used for authenticated request"));
  }

  public static Integer fetchUserId(Principal authentication) {
    return getUserId(authentication).orElseThrow(() ->
        new IllegalStateException("This method should be used for authenticated request"));
  }
}
