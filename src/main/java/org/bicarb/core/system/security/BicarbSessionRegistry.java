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

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;

/**
 * Extend SessionRegistry.
 * @author olOwOlo
 */
public class BicarbSessionRegistry extends SessionRegistryImpl {

  private static final Logger logger = LoggerFactory.getLogger(BicarbSessionRegistry.class);

  /**
   * expireSessionInformationByUsername.
   * @param username username
   */
  public void expireSessionInformationByUsername(String username) {
    List<SessionInformation> sessionInformationList = getAllSessions(
        new User(username, "", Collections.emptyList()), false);

    for (SessionInformation sessionInformation: sessionInformationList) {
      sessionInformation.expireNow();
    }

    logger.debug("Expire " + sessionInformationList + " by username [" + username + "]");
  }
}
