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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author olOwOlo
 */
@WithAnonymousUser
public class AdminTest extends BaseSetup {

  @Autowired
  private UserRepository userRepository;

  @Test
  void testCreateNotFirstUser() throws Exception {
    mockRequest.post(mockMvc, "/api/admin", jsonBody.getJson("/user/post.json"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errors[0].detail").value("admin must be first user"));
  }

  @Sql(statements = "delete from users;")
  @Test
  void testCreateForbidden() throws Exception {
    mockRequest.post(mockMvc, "/api/admin", jsonBody.getJson("/user/post403.json"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errors[0].detail").value("ForbiddenAccessException"));
  }

  @Sql(statements = "delete from users;")
  @Test
  void testCreateSuccess() throws Exception {
    mockRequest.post(mockMvc, "/api/admin", jsonBody.getJson("/user/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.relationships.group.data.id").value("1"))
        .andExpect(jsonPath("$.data.attributes.active").value(true));

    List<User> ul = userRepository.findAll();
    assertThat(ul.size()).isEqualTo(1);
    assertThat(ul.get(0).getGroup().getId()).isEqualTo(1);
    assertThat(ul.get(0).getActive()).isTrue();
  }
}
