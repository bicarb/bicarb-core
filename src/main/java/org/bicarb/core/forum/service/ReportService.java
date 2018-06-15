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

package org.bicarb.core.forum.service;

import com.yahoo.elide.core.ErrorObjects;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.repository.PostRepository;
import org.bicarb.core.forum.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Report Service.
 *
 * @author olOwOlo
 */
@Service
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostRepository postRepository;

  @Autowired
  public ReportService(ReportRepository reportRepository, PostRepository postRepository) {
    this.reportRepository = reportRepository;
    this.postRepository = postRepository;
  }

  /**
   * handleByPostId.
   * @param postId postId
   * @return {@literal Optional<Pair<HttpStatus, ErrorObjects>>}
   */
  @PreAuthorize("hasAuthority('" + PermissionCheck.REPORT_MANAGE + "')")
  @Transactional
  public Optional<Pair<Integer, ErrorObjects>> handleByPostId(Integer postId) {
    Optional<Post> post = postRepository.findById(postId);
    if (post.isPresent()) {
      reportRepository.handleByPost(post.get(), Instant.now());
      return Optional.empty();
    } else {
      return Optional.of(Pair.of(HttpStatus.NOT_FOUND.value(), ErrorObjects.builder()
          .addError().withDetail("Unknown identifier '" + postId + "' for post").build()));
    }
  }
}
