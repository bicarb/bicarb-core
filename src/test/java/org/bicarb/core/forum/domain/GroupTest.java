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
import org.bicarb.core.forum.repository.GroupRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

/**
 * Group Test.
 * @author olOwOlo
 */
@WithUserDetails(value = "admin")
class GroupTest extends BaseSetup {

  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/group", jsonBody.getJson("/group/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes.color").value("#eee"))
        .andExpect(jsonPath("$.data.attributes.icon").value("icon icon-circle"))
        .andExpect(jsonPath("$.data.attributes.name").value("Player"))
        .andExpect(jsonPath("$.data.attributes.permissions.length()").value(3))
        .andExpect(jsonPath("$.data.attributes.permissions[2]").value(3));
  }

  @WithAnonymousUser
  @Test
  void testRead() throws Exception {
    mockRequest.get(mockMvc, "/api/group")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].attributes.permissions").isArray());
  }

  @Test
  void testUpdate() throws Exception {
    mockRequest.patch(mockMvc, "/api/group/1", jsonBody.getJson("/group/patch.json"))
        .andExpect(status().isNoContent());

    Group g = groupRepository.getOne(1);
    assertThat(g.getName()).isEqualTo("Player");
    assertThat(g.getColor()).isEqualTo("#eee");
    assertThat(g.getIcon()).isEqualTo("icon icon-circle");
    assertThat(g.getPermissions().size()).isEqualTo(3);
    assertThat(g.getPermissions()).containsExactlyInAnyOrder("1", "2", "3");
  }

  @Test
  void testDeletePreDefine() throws Exception {
    mockRequest.delete(mockMvc, "/api/group/1").andExpect(status().isForbidden());
    mockRequest.delete(mockMvc, "/api/group/2").andExpect(status().isForbidden());
    mockRequest.delete(mockMvc, "/api/group/3").andExpect(status().isForbidden());
  }

  @Sql(scripts = "/group/prepareDelete.sql")
  @Test
  void tesDeleteNew() throws Exception {
    mockRequest.delete(mockMvc, "/api/group/233").andExpect(status().isNoContent());

    assertThat(userRepository.getOne(22).getGroup().getId()).isEqualTo(3);
    assertThat(userRepository.getOne(33).getGroup().getId()).isEqualTo(3);
  }

  @Sql(scripts = "/group/prepareDelete.sql")
  @WithUserDetails("alice")
  @Test
  void testNoPermission() throws Exception {
    mockRequest.post(mockMvc, "/api/group", jsonBody.getJson("/group/post.json"))
        .andExpect(status().isForbidden());

    mockRequest.patch(mockMvc, "/api/group/1", jsonBody.getJson("/group/patch.json"))
        .andExpect(status().isForbidden());

    mockRequest.delete(mockMvc, "/api/group/233").andExpect(status().isForbidden());
  }
}
