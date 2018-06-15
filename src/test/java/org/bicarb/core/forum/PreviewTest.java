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

package org.bicarb.core.forum;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bicarb.core.BaseSetup;
import org.junit.jupiter.api.Test;

/**
 * @author olOwOlo
 */
public class PreviewTest extends BaseSetup {

  @Test
  void testPreviewTopicBody() throws Exception {
    mockRequest.postJson(mockMvc, "/api/preview", jsonBody.getJson("/preview/topicBody.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body").value("<h1><a href=\"#heading\" id=\"heading\" class=\"anchor-link\"></a>heading</h1>\n<p><a class=\"user-link\" href=\"/user/admin\">@admin </a>hello!</p>\n"));
  }

  @Test
  void testPreviewPost() throws Exception {
    mockRequest.postJson(mockMvc, "/api/preview", jsonBody.getJson("/preview/postBody.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body").value("<h1>heading</h1>\n<p><a class=\"user-link\" href=\"/user/admin\">@admin </a>hello!</p>\n"));
  }

  @Test
  void testInvalid() throws Exception {
    mockRequest.postJson(mockMvc, "/api/preview", "{}")
        .andExpect(status().isBadRequest());
  }
}
