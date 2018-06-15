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

import static org.bicarb.core.MockRequest.JSON_API_CONTENT_TYPE;
import static org.bicarb.core.MockRequest.JSON_API_RESPONSE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bicarb.core.BaseSetup;
import org.junit.jupiter.api.Test;

/**
 * @author olOwOlo
 */
public class CsrfTest extends BaseSetup {

  @Test
  void testSuccess() throws Exception {
    mockMvc.perform(post("/api/post")
        .with(csrf().asHeader())
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(jsonBody.getJson("/post/post.json")))
        .andExpect(status().isCreated());
  }

  @Test
  void testNoCsrf() throws Exception {
    mockMvc.perform(post("/api/post")
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(jsonBody.getJson("/post/post.json")))
        .andExpect(status().isForbidden());
  }

  @Test
  void testInvalidCsrf() throws Exception {
    mockMvc.perform(post("/api/post")
        .with(csrf().asHeader().useInvalidToken())
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(jsonBody.getJson("/post/post.json")))
        .andExpect(status().isForbidden());
  }
}
