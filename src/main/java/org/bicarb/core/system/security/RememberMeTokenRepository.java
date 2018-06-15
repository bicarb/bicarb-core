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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * RememberMeTokenRepository.
 *
 * @author olOwOlo
 */
public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, String> {

  void deleteByUsernameIgnoreCase(String username);

  @Modifying
  @Query("update RememberMeToken set token = ?2, lastUsed = ?3 where series = ?1")
  void updateTokenBySeries(String series, String tokenValue, Instant lastUsed);
}
