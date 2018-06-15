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
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.stream.Collectors;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.repository.CategoryRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

/**
 * User: create, select, update(delete, lock, edit title)
 * Mod: pinned, delete, lock, edit title
 * @author olOwOlo
 */
class TopicTest extends BaseSetup {

  @Autowired
  private TopicRepository topicRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/topic", jsonBody.getJson("/topic/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes.createAt", allOf(startsWith("2"), endsWith("Z"))))
        .andExpect(jsonPath("$.data.attributes.delete").value(false))
        .andExpect(jsonPath("$.data.attributes.feature").value(false))
        .andExpect(jsonPath("$.data.attributes.locked").value(false))
        .andExpect(jsonPath("$.data.attributes.pinned").value(false))
        .andExpect(jsonPath("$.data.attributes.lastReplyAt", allOf(startsWith("2"), endsWith("Z"))))
        .andExpect(jsonPath("$.data.attributes.postIndex").value(0))
        .andExpect(jsonPath("$.data.attributes.slug").value("wo-shi-biao-ti"))
        .andExpect(jsonPath("$.data.attributes.title").value("我是标题！"))
        .andExpect(jsonPath("$.data.relationships.author.data.id").value("1"))
        .andExpect(jsonPath("$.data.relationships.categories.data.length()").value(4))
        .andExpect(jsonPath("$.data.relationships.lastReplyBy.data").value(IsNull.nullValue()));
  }

  @Sql(statements = "update topics set locked = true, locked_by_id = '1' where id = '1';")
  @Test
  void testCreatePostForLocked() throws Exception {
    mockRequest.post(mockMvc, "/api/post", jsonBody.getJson("/post/post.json"))
        .andExpect(status().isForbidden());
  }

  @Sql(statements = "update topics set delete = true, delete_by_id = '1' where id = '1';")
  @Test
  void testCreatePostForDelete() throws Exception {
    mockRequest.post(mockMvc, "/api/post", jsonBody.getJson("/post/post.json"))
        .andExpect(status().isForbidden());
  }

  @Sql(statements = "update topics set delete = true, delete_by_id = '3' where id = '1';")
  @Test
  void testReadDeleteByOther() throws Exception {
    mockRequest.get(mockMvc, "/api/topic/1").andExpect(status().isNotFound());  // filter
    mockRequest.get(mockMvc, "/api/topic?filter=categories.id==0")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Sql(statements = "update topics set delete = true, delete_by_id = '1' where id = '1';")
  @Test
  void testReadDeleteByOwner() throws Exception {
    mockRequest.get(mockMvc, "/api/topic/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.delete").value(true));
    mockRequest.get(mockMvc, "/api/topic?filter=categories.id==0&page[totals]")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.meta.page.totalRecords").value(1));
  }

  @Test
  void testUpdateTitle() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchTitle.json"))
        .andExpect(status().isNoContent());

    Topic newTopic =  topicRepository.getOne(1);
    assertThat(newTopic.getTitle()).isEqualTo("新标题");
    assertThat(newTopic.getSlug()).isEqualTo("xin-biao-ti");
  }

  @Test
  void testUpdateCategoriesByJsonApi() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchCategories403.json"))
        .andExpect(status().isForbidden());
  }

  @WithUserDetails("alice")
  @Test
  void testUpdateCategoriesNotOwner() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/topic/1/categories", "categoryId=1")
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateCategoriesWrongIdentifier() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/topic/666/categories", "categoryId=1")
        .andExpect(status().isNotFound());
    mockRequest.patchForm(mockMvc, "/api/topic/1/categories", "categoryId=666")
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateCategories() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/topic/1/categories", "categoryId=1")
        .andExpect(status().isNoContent());

    Map<Integer, Category> cm = categoryRepository.findAll().stream().collect(Collectors.toMap(Category::getId, c -> c));
    assertThat(cm.get(0).getTopicCount()).isEqualTo(1);
    assertThat(cm.get(1).getTopicCount()).isEqualTo(1);
    assertThat(cm.get(4).getTopicCount()).isEqualTo(0);
    assertThat(cm.get(5).getTopicCount()).isEqualTo(0);

    Topic t =  topicRepository.getOne(1);
    assertThat(t.getCategories()).containsExactlyInAnyOrder(cm.get(0), cm.get(1));
  }

  @Test
  void testUpdateLocked() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchLocked.json"))
        .andExpect(status().isNoContent());
    Topic t = topicRepository.getOne(1);
    assertThat(t.getLocked()).isTrue();
    assertThat(t.getLockedBy().getId()).isEqualTo(1);
  }

  @Sql(statements = "update topics set locked = true, locked_by_id = '3' where id = '1';")
  @Test
  void testUpdateUnlockByOther() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchUnlock.json"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateDelete() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchDelete.json"))
        .andExpect(status().isNoContent());
    Topic t = topicRepository.getOne(1);
    assertThat(t.getDelete()).isTrue();
    assertThat(t.getDeleteBy().getId()).isEqualTo(1);
  }

  @Sql(statements = "update topics set delete = true, delete_by_id = '3' where id = '1';")
  @Test
  void testUpdateRestoreByOther() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchRestore.json"))
        .andExpect(status().isNotFound());
  }

  @WithUserDetails("mod")
  @Test
  void testUpdatePinned() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchPinned.json"))
        .andExpect(status().isNoContent());
    assertThat(topicRepository.getOne(1).getPinned()).isTrue();
  }

  @WithUserDetails("mod")
  @Test
  void testUpdateFeature() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchFeature.json"))
        .andExpect(status().isNoContent());
    assertThat(topicRepository.getOne(1).getFeature()).isTrue();
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/topic/1").andExpect(status().isForbidden());
  }

  @Test
  void testNoPermission() throws Exception {
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchPinned.json"))
        .andExpect(status().isForbidden());
    mockRequest.patch(mockMvc, "/api/topic/1", jsonBody.getJson("/topic/patchFeature.json"))
        .andExpect(status().isForbidden());
  }
}
