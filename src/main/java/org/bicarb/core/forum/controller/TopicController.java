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

package org.bicarb.core.forum.controller;

import com.yahoo.elide.core.ErrorObjects;
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
import org.bicarb.core.forum.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TopicController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class TopicController {


  private final TopicService topicService;

  @Autowired
  public TopicController(TopicService topicService) {
    this.topicService = topicService;
  }

  @ApiOperation(value = "Update topic categories",
      notes = "All parents of the category will be added automatically.")
  @PatchMapping(value = "/topic/{topicId}/categories", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErrorObjects> patchCategories(
      @PathVariable Integer topicId,
      @RequestParam Integer categoryId,
      Principal authentication) {

    return topicService.patchCategories(topicId, categoryId, authentication)
        .map(pair ->  ResponseEntity.status(pair.getLeft()).body(pair.getRight()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
  }
}
