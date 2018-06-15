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
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SearchController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class SearchController {

  private final SearchService searchService;

  @Autowired
  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> search(
      @RequestParam String q,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      Principal auth) {
    ElideResponse elideResponse = searchService
        .search(q, PageRequest.of(page - 1, size), auth);
    return ResponseEntity.status(elideResponse.getResponseCode()).body(elideResponse.getBody());
  }

  @GetMapping(value = "/search/{postId}/relate", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> searchRelate(
      @PathVariable Integer postId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      Principal auth) {
    ElideResponse elideResponse = searchService
        .searchMoreLikeThis(postId, PageRequest.of(page - 1, size), auth);
    return ResponseEntity.status(elideResponse.getResponseCode()).body(elideResponse.getBody());
  }

  @ApiOperation(value = "Whether rebuild is processing", notes = "true or false")
  @PreAuthorize("hasAuthority('admin')")
  @GetMapping(value = "/index/building", produces = MediaType.APPLICATION_JSON_VALUE)
  public Boolean isIndexing() {
    return searchService.isIndexing();
  }

  @ApiOperation(value = "Rebuild index",
      notes = "if safe, then update all indexes, else delete all and rebuild")
  @PreAuthorize("hasAuthority('admin')")
  @PostMapping(value = "/index/rebuild")
  public void reBuildIndex(@RequestParam(required = false) Boolean safe) {
    if (safe != null && safe) {
      searchService.safeReBuildIndex();
    } else {
      searchService.reBuildIndex();
    }
  }
}
