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

import java.time.Instant;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Report Repository.
 *
 * @author olOwOlo
 */
public interface ReportRepository extends JpaRepository<Report, Integer> {

  @Modifying
  @Query("update Report r set r.handleAt = ?2 where r.post = ?1")
  void handleByPost(Post post, Instant now);
}
