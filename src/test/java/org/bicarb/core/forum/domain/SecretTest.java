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

package org.bicarb.core.forum.domain;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bicarb.core.BaseSetup;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * SecretTest.
 *
 * @author olOwOlo
 */
public class SecretTest extends BaseSetup {

  @WithUserDetails("admin")
  @Test
  void testRead() throws Exception {
    mockRequest.get(mockMvc, "/api/secret")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(7));
  }

  @WithUserDetails("mod")
  @Test
  void testReadNoPermission() throws Exception {
    mockRequest.get(mockMvc, "/api/secret")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }
}
