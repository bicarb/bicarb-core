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
import org.bicarb.core.TimeAssert;
import org.bicarb.core.forum.repository.PostRepository;
import org.bicarb.core.forum.repository.TopicRepository;
import org.hamcrest.collection.IsIn;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

/**
 * User: create, select, update(delete, edit)
 * Mod: delete, edit
 * @author olOwOlo
 */
class PostTest extends BaseSetup {

  @Autowired
  private PostRepository postRepository;
  @Autowired
  private TopicRepository topicRepository;

  @WithUserDetails("alice")
  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/post", jsonBody.getJson("/post/post.json"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.attributes.index").value(5))
        .andExpect(jsonPath("$.data.attributes.raw").value("@admin **Hello**"))
        .andExpect(jsonPath("$.data.attributes.delete").value(false))
        .andExpect(jsonPath("$.data.attributes.ip").doesNotExist())
        .andExpect(jsonPath("$.data.attributes.lastEditAt").value(IsNull.nullValue()))
        .andExpect(jsonPath("$.data.attributes.createAt").isString())
        .andExpect(jsonPath("$.data.relationships.author.data.id").value("5"))
        .andExpect(jsonPath("$.data.relationships.topic.data.id").value("1"));

    Topic t = topicRepository.getOne(1);
    assertThat(t.getPostIndex()).isEqualTo(5);
    assertThat(t.getLastReplyBy().getId()).isEqualTo(5);
    assertThat(postRepository.findAll().size()).isEqualTo(6);
  }

  @Sql(statements = "update posts set delete = true, delete_by_id = '3' where id = '1';")
  @Test
  void testReadDeleteByOther() throws Exception {
    mockRequest.get(mockMvc, "/api/post/1").andExpect(status().isNotFound());  // filter
    mockRequest.get(mockMvc, "/api/post?filter[post]=topic.id==1&page[totals]")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.meta.page.totalRecords").value(4));
  }

  @Sql(statements = "update posts set delete = true, delete_by_id = '1' where id = '1';")
  @Test
  void testReadDeleteByOwner() throws Exception {
    mockRequest.get(mockMvc, "/api/post/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.delete").value(true));
  }

  @Sql(statements = "update topics set delete = true, delete_by_id = '1' where id = '1';")
  @Test
  void testReadTopicDeleteByOwner() throws Exception {
    mockRequest.get(mockMvc, "/api/post/1").andExpect(status().isNotFound());
    mockRequest.get(mockMvc, "/api/post?filter[post]=topic.id==1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @WithUserDetails("mod")
  @Test
  void testReadIp() throws Exception {
    mockRequest.get(mockMvc, "/api/post/1")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.ip").value(IsIn.isOneOf("127.0.0.1", "0:0:0:0:0:0:0:1")));
  }

  @Test
  void testUpdateRawTopicBody() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchRawTopicBody.json"))
        .andExpect(status().isNoContent());

    Post p = postRepository.getOne(1);
    assertThat(p.getRaw()).isEqualTo("## bla bla bla\n\n@alice @bob @unknown Hello.");
    assertThat(p.getCooked()).isEqualTo("<h2><a href=\"#bla-bla-bla\" id=\"bla-bla-bla\" class=\"anchor-link\"></a>bla bla bla</h2>\n<p><a class=\"user-link\" href=\"/user/alice\">@alice </a><a class=\"user-link\" href=\"/user/bob\">@bob </a>@unknown Hello.</p>\n");
    TimeAssert.assertNow(p.getLastEditAt());
  }

  @Test
  void testUpdateRawPostBody() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/2", jsonBody.getJson("/post/patchRawPostBody.json"))
        .andExpect(status().isNoContent());

    Post p = postRepository.getOne(2);
    assertThat(p.getRaw()).isEqualTo("## bla bla bla\n\n@alice @bob @unknown Hello.");
    assertThat(p.getCooked()).isEqualTo("<h2>bla bla bla</h2>\n<p><a class=\"user-link\" href=\"/user/alice\">@alice </a><a class=\"user-link\" href=\"/user/bob\">@bob </a>@unknown Hello.</p>\n");
    TimeAssert.assertNow(p.getLastEditAt());
  }

  @WithUserDetails("alice")
  @Test
  void testUpdateRawNotOwner() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchRestore.json"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateDelete() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchDelete.json"))
        .andExpect(status().isNoContent());

    Post p = postRepository.getOne(1);
    assertThat(p.getDelete()).isTrue();
    assertThat(p.getDeleteBy().getId()).isEqualTo(1);
  }

  @Sql(statements = "update posts set delete = true, delete_by_id = '1' where id = '1';")
  @Test
  void testUpdateRestoreByOwner() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchRestore.json"))
        .andExpect(status().isNoContent());

    Post p = postRepository.getOne(1);
    assertThat(p.getDelete()).isFalse();
    assertThat(p.getDeleteBy().getId()).isEqualTo(1);
  }

  @Sql(statements = "update posts set delete = true, delete_by_id = '3' where id = '1';")
  @Test
  void testUpdateRestoreByOther() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchRestore.json"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/post/1").andExpect(status().isForbidden());
  }
}
