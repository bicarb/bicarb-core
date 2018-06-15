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
import org.bicarb.core.TimeAssert;
import org.bicarb.core.forum.repository.ReportRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Report Test.
 *
 * @author olOwOlo
 */
@WithUserDetails("mod")
class ReportTest extends BaseSetup {

  @Autowired
  private ReportRepository reportRepository;

  @WithUserDetails("alice")
  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/report", jsonBody.getJson("/report/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes").doesNotExist())
        .andExpect(jsonPath("$.data.id").isString())
        .andExpect(jsonPath("$.data.type").value("report"));
  }

  @Test
  void testRead() throws Exception {
    mockRequest.get(mockMvc, "/api/report?sort=id")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].attributes.reason").value("reason"))
        .andExpect(jsonPath("$.data[0].attributes.createAt").isString())
        .andExpect(jsonPath("$.data[0].attributes.handleAt").value(IsNull.nullValue()))
        .andExpect(jsonPath("$.data[0].relationships.by.data.id").value(1))
        .andExpect(jsonPath("$.data[0].relationships.post.data.id").value(1));
  }

  @Test
  void testUpdateHandle() throws Exception {
    mockRequest.patchNoBody(mockMvc, "/api/post/1/report/handle")
        .andExpect(status().isNoContent());

    TimeAssert.assertNow(reportRepository.getOne(1).getHandleAt());
    TimeAssert.assertNow(reportRepository.getOne(2).getHandleAt());
  }

  @Test
  void testUpdateHandleInvalid() throws Exception {
    mockRequest.patchNoBody(mockMvc, "/api/post/666/report/handle")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].detail").value("Unknown identifier '666' for post"));
  }

  @WithUserDetails("alice")
  @Test
  void testUpdateNoPermission() throws Exception {
    mockRequest.patchNoBody(mockMvc, "/api/post/1/report/handle")
        .andExpect(status().isForbidden());
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/report/1")
        .andExpect(status().isForbidden());
  }
}
