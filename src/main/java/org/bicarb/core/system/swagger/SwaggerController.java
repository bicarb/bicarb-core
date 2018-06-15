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

package org.bicarb.core.system.swagger;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

/**
 * SwaggerController.
 *
 * @author olOwOlo
 */
@ApiIgnore
@Controller
public class SwaggerController {

  private final SwaggerService swaggerService;

  @Autowired
  public SwaggerController(SwaggerService swaggerService) {
    this.swaggerService = swaggerService;
  }

  @ResponseBody
  @GetMapping(value = "/v2/api-docs/elide", produces = MediaType.APPLICATION_JSON_VALUE)
  public String elideDoc() throws IOException {
    return swaggerService.getElideDoc();
  }

  @GetMapping("/api")
  public String redirectToDoc() {
    return "redirect:/swagger-ui.html";
  }
}
