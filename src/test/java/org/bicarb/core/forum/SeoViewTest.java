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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bicarb.core.BaseSetup;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * @author olOwOlo
 */
public class SeoViewTest extends BaseSetup {

  @Test
  void testTopicList() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("settings", "categories", "groups"))
        .andExpect(model().attributeExists("auth"))
        .andExpect(model().attributeExists("page", "topics"));
  }

  @Test
  void testTopic() throws Exception {
    mockMvc.perform(get("/topic/1/topic-slug"))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("settings", "categories", "groups"))
        .andExpect(model().attributeExists("auth"))
        .andExpect(model().attributeExists("page", "topic", "posts"));
  }

  @Test
  void testCommon() throws Exception {
    mockMvc.perform(get("/not-define"))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("settings", "categories", "groups"))
        .andExpect(model().attributeExists("auth"));
  }

  @WithAnonymousUser
  @Test
  void testCommonAnonymous() throws Exception {
    mockMvc.perform(get("/not-define"))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("settings", "categories", "groups"))
        .andExpect(model().attributeDoesNotExist("auth"));
  }
}
