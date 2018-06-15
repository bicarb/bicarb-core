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

import java.time.Instant;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.TimeAssert;
import org.bicarb.core.forum.repository.UserRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * User: SignIn √, create √, read √.
 * update properties(nickname, emailPublic, bio...) √, change password √.
 * Mod: lock √
 * Admin: change group √
 * @author olOwOlo
 */
class UserTest extends BaseSetup {

  @Autowired
  private UserRepository userRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/user", jsonBody.getJson("/user/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes.username").value("test"))
        .andExpect(jsonPath("$.data.attributes.nickname").value("瞄瞄"))
        .andExpect(jsonPath("$.data.attributes.email").doesNotExist())
         .andExpect(jsonPath("$.data.attributes.active").value(false))
        .andExpect(jsonPath("$.data.relationships.group.data.id").value(3))
        .andExpect(jsonPath("$.data.attributes.password").doesNotExist());
  }

  @Test
  void testCreateDuplicated() throws Exception {
    mockRequest.post(mockMvc, "/api/user", jsonBody.getJson("/user/post.json"));
    mockRequest.post(mockMvc, "/api/user", jsonBody.getJson("/user/post.json"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors.length()").value(3))
        .andExpect(jsonPath("$.errors[0].detail").isString())
        .andExpect(jsonPath("$.errors[0].code").isString());
  }

  @Test
  void testCreateSecurity() throws Exception {
    mockRequest.post(mockMvc, "/api/user", jsonBody.getJson("/user/post403.json"))
        .andExpect(status().isForbidden());
  }

  @WithAnonymousUser
  @Test
  void testReadAnonymous() throws Exception {
    mockRequest.get(mockMvc, "/api/user/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.password").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.email").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.lastSignIp").doesNotExist());
  }

  @Test
  void testReadOwn() throws Exception {
    mockRequest.get(mockMvc, "/api/user/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.password").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.email").exists())
        .andExpect(jsonPath("$.data.attributes.lastSignIp").doesNotExist());
  }

  @WithUserDetails("mod")
  @Test
  void testReadMod() throws Exception {
    mockRequest.get(mockMvc, "/api/user/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.password").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.email").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.lastSignIp").value(IsNull.nullValue()));
  }

  @Test
  void testUpdateSecurity() throws Exception {
    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchLockedUntil.json"))
        .andExpect(status().isForbidden());

    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchGroup.json"))
        .andExpect(status().isForbidden());
  }

  @WithUserDetails("alice")
  @Test
  void testUpdateNotOwnProperty() throws Exception {
    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchOwnAttr.json"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateOwnProperty() throws Exception {
    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchOwnAttr.json"))
        .andExpect(status().isNoContent());

    User u = userRepository.getOne(1);
    assertThat(u.getEmailPublic()).isTrue();
    assertThat(u.getBio()).isEqualTo("bio");
    assertThat(u.getWebsite()).isEqualTo("https://github.com/olOwOlo");
    assertThat(u.getGithub()).isEqualTo("olOwOlo");
  }

  @WithUserDetails(value = "admin")
  @Test
  void testUpdateGroup() throws Exception {
    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchGroup.json"))
        .andExpect(status().isNoContent());

    User u = userRepository.getOne(1);
    assertThat(u.getGroup().getId()).isEqualTo(2);
  }

  @WithUserDetails(value = "mod")
  @Test
  void testUpdateLock() throws Exception {
    mockRequest.patch(mockMvc, "/api/user/1", jsonBody.getJson("/user/patchLockedUntil.json"))
        .andExpect(status().isNoContent());

    User u = userRepository.getOne(1);
    TimeAssert.assertNow(u.getLockedAt());
    assertThat(u.getLockedUntil()).isEqualTo(Instant.parse("9999-12-31T23:59:59.999999999Z"));
  }

  @WithUserDetails("admin")
  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/user/1").andExpect(status().isForbidden());
  }

  @WithUserDetails("inactive")
  @Test
  void testInvalidUserInactive() throws Exception {
    mockRequest.post(mockMvc, "/api/topic", jsonBody.getJson("/topic/post.json"))
        .andExpect(status().isForbidden());
  }

  @WithUserDetails("locked")
  @Test
  void testInvalidUserLocked() throws Exception {
    mockRequest.post(mockMvc, "/api/topic", jsonBody.getJson("/topic/post.json"))
        .andExpect(status().isForbidden());
  }
}
