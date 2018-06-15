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

package org.bicarb.core.forum.search;

import com.yahoo.elide.ElideResponse;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Pageable;

/**
 * SearchService.
 *
 * @author olOwOlo
 */
public interface SearchService {

  /**
   * Full text search, query also support the following syntax.
   * <ol>
   *   <li>boolean (AND using +, OR using |, NOT using -)</li>
   *   <li>prefix (prefix*)</li>
   *   <li>phrase ("some phrase")</li>
   *   <li>precedence (using parentheses)</li>
   *   <li>fuzzy (fuzy~2)</li>
   *   <li>near operator for phrase queries ("war peace"~3).</li>
   * </ol>
   */
  ElideResponse search(String query, Pageable pageable, Principal auth);

  /**
   * searchMoreLikeThis.
   */
  ElideResponse searchMoreLikeThis(Integer postId, Pageable pageable, Principal auth);

  /**
   * Create or update all. Async.
   * @return true if success, otherwise return false
   */
  CompletableFuture<Boolean> safeReBuildIndex();

  /**
   * Delete all, then rebuild. Async.
   * @return true if success, otherwise return false
   */
  CompletableFuture<Boolean> reBuildIndex();

  /**
   * Whether rebuild is processing.
   */
  Boolean isIndexing();
}
