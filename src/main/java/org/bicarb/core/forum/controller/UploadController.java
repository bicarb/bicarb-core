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
import java.io.IOException;
import java.security.Principal;
import org.bicarb.core.forum.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class UploadController {

  private final UploadService uploadService;

  @Autowired
  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @ApiOperation(value = "Upload file",
      notes = "Return the relative path of the file, default directory is /upload/image")
  @PostMapping(value = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> upload(@RequestPart MultipartFile file) throws IOException {

    return uploadService.saveImage(file)
        .map(path -> ResponseEntity.status(HttpStatus.CREATED).body(path))
        .orElseGet(() -> ResponseEntity.badRequest().body("unsupported file type"));
  }

  @ApiOperation(value = "Upload user avatar",
      notes = "Return the relative path of the file, default directory is /upload/avatar")
  @PostMapping(value = "/upload/avatar", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> uploadAvatar(
      @RequestPart MultipartFile file, Principal auth) throws IOException {

    return uploadService.saveAvatar(file, auth)
        .map(path -> ResponseEntity.status(HttpStatus.CREATED).body(path))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }
}
