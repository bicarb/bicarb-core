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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.repository.SettingRepository;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * @author olOwOlo
 */
@WithUserDetails(value = "admin")
class SettingTest extends BaseSetup {

  @Autowired
  private SettingRepository settingRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/setting", jsonBody.getJson("/setting/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value("key"))
        .andExpect(jsonPath("$.data.attributes.value").value("value"));
  }

  @WithAnonymousUser
  @Test
  void testRead() throws Exception {
    mockRequest.get(mockMvc, "/api/setting?sort=id")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[1].id", StringContains.containsString("Color")))
        .andExpect(jsonPath("$.data[1].attributes.value").value("#fff"));
  }

  @Test
  void testUpdate() throws Exception {
    mockRequest.patch(mockMvc, "/api/setting/primaryColor", jsonBody.getJson("/setting/patch.json"))
        .andExpect(status().isNoContent());

    assertThat(settingRepository.getOne("primaryColor").getValue()).isEqualTo("#000");
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/setting/primaryColor")
        .andExpect(status().isNoContent());

    assertThat(settingRepository.existsById("primaryColor")).isFalse();
  }

  @WithUserDetails("alice")
  @Test
  void testNoPermission() throws Exception {
    mockRequest.post(mockMvc, "/api/setting", jsonBody.getJson("/setting/post.json"))
        .andExpect(status().isForbidden());

    mockRequest.patch(mockMvc, "/api/setting/primaryColor", jsonBody.getJson("/setting/patch.json"))
        .andExpect(status().isForbidden());

    mockRequest.delete(mockMvc, "/api/setting/primaryColor")
        .andExpect(status().isForbidden());
  }
}
