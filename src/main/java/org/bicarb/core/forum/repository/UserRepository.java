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

package org.bicarb.core.forum.repository;

import java.util.Optional;
import javax.persistence.QueryHint;
import org.bicarb.core.forum.domain.Group;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.Constant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

/**
 * UserRepository.
 *
 * @author olOwOlo
 */
public interface UserRepository extends JpaRepository<User, Integer> {

  @QueryHints({ @QueryHint(name = Constant.QUERY_CACHE_HINT, value = "true") })
  Optional<User> findByUsernameIgnoreCase(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByNickname(String nickname);

  @Modifying
  @Query("update User u set u.group = ?2 where u.group = ?1")
  void updateUserGroup(Group oldG, Group newG);
}
