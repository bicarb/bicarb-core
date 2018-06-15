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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yahoo.elide.ElideResponse;
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
import org.bicarb.core.forum.service.AdminService;
import org.bicarb.core.system.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class AdminController {

  private final AdminService adminService;

  @Autowired
  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @ApiOperation(value = "Create an admin account", notes = "This request will be forwarded to "
      + "/api/user and then change the group to admin. Note: Admin must be the first user.")
  @PostMapping(value = "/admin",
      consumes = Constant.JSON_API_CONTENT_TYPE, produces = Constant.JSON_API_CONTENT_TYPE)
  public ResponseEntity<String> createAdmin(@RequestBody String requestBody, Principal auth)
      throws JsonProcessingException {
    ElideResponse elideResponse = adminService.registerAdmin(requestBody, auth);
    return ResponseEntity.status(elideResponse.getResponseCode()).body(elideResponse.getBody());
  }
}
