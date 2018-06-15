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

import io.swagger.annotations.ApiOperation;
import org.bicarb.core.forum.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Category Controller.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class CategoryController {

  private final CategoryService categoryService;

  @Autowired
  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * [PATCH][/category/{categoryId}/location].
   */
  @ApiOperation(value = "Update category position and/or parent",
      notes = "Location is not guaranteed to be continuous.")
  @PatchMapping(value = "/category/{categoryId}/location")
  public ResponseEntity<String> patchLocation(
      @PathVariable Integer categoryId,
      @RequestParam(required = false) Integer position,
      @RequestParam(required = false) Integer parentId) {

    categoryService.patchLocation(categoryId, position, parentId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
