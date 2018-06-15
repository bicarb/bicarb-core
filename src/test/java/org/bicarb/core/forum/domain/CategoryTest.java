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

import java.util.Map;
import java.util.stream.Collectors;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Category Test.
 *
 * @author olOwOlo
 */
@WithUserDetails(value = "admin")
class CategoryTest extends BaseSetup {

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private TopicRepository topicRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/category", jsonBody.getJson("/category/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes.position").value(3))
        .andExpect(jsonPath("$.data.attributes.slug").value("qiu-zhu"));
  }

  @Test
  void testCreateConflict() throws Exception {
    mockRequest.post(mockMvc, "/api/category", jsonBody.getJson("/category/post.json"));
    mockRequest.post(mockMvc, "/api/category", jsonBody.getJson("/category/post.json"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors.length()").value(1))
        .andExpect(jsonPath("$.errors[0].code").value("4094"))
        .andExpect(jsonPath("$.errors[0].detail").isString());
  }

  @WithAnonymousUser
  @Test
  void testRead() throws Exception {
    mockRequest.get(mockMvc, "/api/category")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(6));
  }

  @Test
  void testUpdateProperty() throws Exception {
    mockRequest.patch(mockMvc, "/api/category/1", jsonBody.getJson("/category/patchProperty.json"))
        .andExpect(status().isNoContent());

    Category c = categoryRepository.getOne(1);
    assertThat(c.getName()).isEqualTo("Game Game");
    assertThat(c.getSlug()).isEqualTo("game-game");
    assertThat(c.getDescription()).isEqualTo("Game");
  }

  @Test
  void testUpdateLocationInvalid() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/category/4/location", "")
        .andExpect(status().isUnprocessableEntity());
    mockRequest.patchForm(mockMvc, "/api/category/233/location", "parentId=0&position=0")
        .andExpect(status().isNotFound());
    mockRequest.patchForm(mockMvc, "/api/category/4/location", "parentId=233&position=0")
        .andExpect(status().isNotFound());
    mockRequest.patchForm(mockMvc, "/api/category/4/location", "parentId=4&position=0")
        .andExpect(status().isUnprocessableEntity());
    mockRequest.patchForm(mockMvc, "/api/category/4/location", "parentId=5&position=0")
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void testUpdateLocationParent() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/category/5/location", "parentId=0")
        .andExpect(status().isNoContent());

    Map<Integer, Category> cm = categoryRepository.findAll().stream().collect(Collectors.toMap(Category::getId, c -> c));
    assertThat(cm.get(5).getParent().getId()).isEqualTo(0);
    assertThat(cm.get(5).getPosition()).isEqualTo(3);
    // count
    assertThat(cm.get(0).getTopicCount()).isEqualTo(1);
    assertThat(cm.get(1).getTopicCount()).isEqualTo(0);
    assertThat(cm.get(4).getTopicCount()).isEqualTo(0);
    assertThat(cm.get(5).getTopicCount()).isEqualTo(1);

    assertThat(topicRepository.getOne(1).getCategories()).containsExactlyInAnyOrder(cm.get(0), cm.get(5));
  }

  @Test
  void testUpdateLocationPosition() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/category/1/location", "position=2")
        .andExpect(status().isNoContent());

    Map<Integer, Category> cm = categoryRepository.findAll().stream().collect(Collectors.toMap(Category::getId, c -> c));
    assertThat(cm.get(1).getParent().getId()).isEqualTo(0);
    assertThat(cm.get(1).getPosition()).isEqualTo(2);
    assertThat(cm.get(2).getPosition()).isEqualTo(1);
    assertThat(cm.get(3).getPosition()).isEqualTo(3);
  }

  @Test
  void testUpdateLocation() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/category/4/location", "parentId=0&position=0")
        .andExpect(status().isNoContent());

    Map<Integer, Category> cm = categoryRepository.findAll().stream().collect(Collectors.toMap(Category::getId, c -> c));
    assertThat(cm.get(4).getParent().getId()).isEqualTo(0);
    assertThat(cm.get(4).getPosition()).isEqualTo(0);
    // count
    assertThat(cm.get(0).getTopicCount()).isEqualTo(1);
    assertThat(cm.get(1).getTopicCount()).isEqualTo(0);
    assertThat(cm.get(4).getTopicCount()).isEqualTo(1);
    assertThat(cm.get(5).getTopicCount()).isEqualTo(1);
    // position
    assertThat(cm.get(1).getPosition()).isEqualTo(1);
    assertThat(cm.get(2).getPosition()).isEqualTo(2);
    assertThat(cm.get(3).getPosition()).isEqualTo(3);

    Topic t = topicRepository.getOne(1);
    assertThat(t.getCategories()).containsExactlyInAnyOrder(cm.get(0), cm.get(4), cm.get(5));
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/category/1")
        .andExpect(status().isNoContent());

    assertThat(categoryRepository.existsById(1)).isFalse();

    Category c = categoryRepository.getOne(4);
    assertThat(c.getParent().getSlug()).isEqualTo("home");
    assertThat(c.getPosition()).isEqualTo(3);

    assertThat(topicRepository.getOne(1).getCategories().size()).isEqualTo(3);
  }

  @WithUserDetails("alice")
  @Test
  void testNoPermission() throws Exception {
    mockRequest.post(mockMvc, "/api/category", jsonBody.getJson("/category/post.json"))
        .andExpect(status().isForbidden());

    mockRequest.patch(mockMvc, "/api/category/1", jsonBody.getJson("/category/patchProperty.json"))
        .andExpect(status().isForbidden());

    mockRequest.delete(mockMvc, "/api/category/1")
        .andExpect(status().isForbidden());

    mockRequest.patchForm(mockMvc, "/api/category/4/location", "parentId=0&position=0")
        .andExpect(status().isForbidden());
  }
}
