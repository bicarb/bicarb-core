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

import com.nimbusds.jose.JOSEException;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.core.ErrorObjects;
import io.swagger.annotations.ApiOperation;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Map;
import javax.mail.MessagingException;
import org.bicarb.core.forum.service.UserService;
import org.bicarb.core.system.Constant;
import org.bicarb.core.system.validation.ValidEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Controller.
 * @author olOwOlo
 */
@Validated
@RequestMapping("/api")
@RestController
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  // "/user/avatar" in UploadController

  @PatchMapping(value = "/user/password", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErrorObjects> patchPassword(
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      Principal authentication) {

    return userService.patchPassword(newPassword, confirmPassword, authentication)
        .map(pair -> ResponseEntity.status(pair.getLeft()).body(pair.getRight()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
  }

  @ApiOperation(value = "send active email",
      notes = "ok: 200, already active: 400")
  @PostMapping("/user/email/verify/send")
  public ResponseEntity<ErrorObjects> sendActiveEmail(Principal authentication)
      throws UnsupportedEncodingException, MessagingException, JOSEException {

    return userService.sendActiveEmail(authentication)
        .map(pair -> ResponseEntity.status(pair.getLeft()).body(pair.getRight()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.OK).build());
  }

  // activeUserByJwe in another controller.

  @PostMapping("/user/password/reset/send")
  public void sendResetPasswordJweEmail(@RequestParam @ValidEmail String email)
      throws JOSEException, UnsupportedEncodingException, MessagingException {

    userService.sendResetPasswordJwe(email);
  }

  @PostMapping("/user/password/reset")
  public ResponseEntity<ErrorObjects> resetPassword(
      @RequestParam String jwe, @RequestParam String newPw) throws JOSEException {

    return userService.resetPassword(jwe, newPw)
        .map(pair -> ResponseEntity.status(pair.getLeft()).body(pair.getRight()))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.OK).build());
  }

  @ApiOperation("Get user list is not allowed.")
  @GetMapping(value = "/user", produces = Constant.JSON_API_CONTENT_TYPE)
  public ResponseEntity<ErrorObjects> getAllUser() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorObjects.builder()
        .addError().withDetail("Access Forbidden")
        .build());
  }

  @ApiOperation("Workaround for '/api/user?filter[user]=username==:username'")
  @GetMapping(value = "/user/{idOrUsername}", produces = Constant.JSON_API_CONTENT_TYPE)
  public ResponseEntity<String> getUserByIdOrUsername(
      @PathVariable String idOrUsername, @RequestParam Map<String, String> params, Principal auth) {
    ElideResponse response = userService.getUserByIdOrUsername(idOrUsername, params, auth);
    return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
  }
}
