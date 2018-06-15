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
import io.swagger.annotations.ApiOperation;
import org.bicarb.core.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * UserViewController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@Controller
public class UserViewController {

  private final UserService userService;

  @Autowired
  public UserViewController(UserService userService) {
    this.userService = userService;
  }

  @ApiOperation(value = "Active user by jwe token",
      notes = "ModelAndView('activeUser', {'state': 'success' | 'alreadyActive' | 'invalidJwe'})")
  @GetMapping(value = "/user/email/verify/{jwe}")
  public ModelAndView activeUserByJwe(@PathVariable String jwe) throws JOSEException {

    return userService.activeUserByJwe(jwe);
  }
}
