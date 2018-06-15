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

import java.util.List;
import java.util.stream.Collectors;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.TimeAssert;
import org.bicarb.core.forum.domain.Notification.NotificationType;
import org.bicarb.core.forum.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * NotificationTest.
 *
 * @author olOwOlo
 */
@WithUserDetails("alice")
class NotificationTest extends BaseSetup {

  @Autowired
  private NotificationRepository notificationRepository;

  @Test
  void testCreate() throws Exception {
    mockRequest.post(mockMvc, "/api/notification", jsonBody.getJson("/notification/post403.json"))
        .andExpect(status().isForbidden());
  }

  @WithUserDetails
  @Test
  void testReadOther() throws Exception {
    mockRequest.get(mockMvc, "/api/notification")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void testReadOwner() throws Exception {
    mockRequest.get(mockMvc, "/api/notification")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  void testUpdateReadAtOwner() throws Exception {
    mockRequest.patch(mockMvc, "/api/notification/1", jsonBody.getJson("/notification/patchReadAt.json"))
        .andExpect(status().isNoContent());

    Notification n = notificationRepository.getOne(1);
    assertThat(n.getReadAt()).isEqualTo("2017-12-31T23:59:59.999999999Z");
  }

  @Test
  void testDelete() throws Exception {
    mockRequest.delete(mockMvc, "/api/notification/1")
        .andExpect(status().isForbidden());
  }

  // ---

  @WithUserDetails
  @Test
  void testReplyInOwnerTopicAndMention() throws Exception {
    mockRequest.post(mockMvc, "/api/post", jsonBody.getJson("/post/post.json"))
        .andExpect(status().isCreated());

    List<Notification> ns = notificationRepository.findAll(Sort.by("id"));
    assertThat(ns.size()).isEqualTo(2);

    Notification n = ns.get(1);
    assertThat(n.getType()).isEqualByComparingTo(NotificationType.MENTION);
    assertThat(n.getSend().getId()).isEqualTo(1);
    assertThat(n.getTo().getId()).isEqualTo(3);
    assertThat(n.getTopic().getId()).isEqualTo(1);
    TimeAssert.assertNow(n.getCreateAt());
  }

  @Test
  void testReplyInOtherTopicAndMention() throws Exception {
    mockRequest.post(mockMvc, "/api/post", jsonBody.getJson("/post/post.json"))
        .andExpect(status().isCreated());

    List<Notification> ns = notificationRepository.findAll(Sort.by("id"));
    assertThat(ns.size()).isEqualTo(3);

    ns = ns.stream().filter(n -> n.getType().equals(NotificationType.REPLY)).collect(Collectors.toList());
    assertThat(ns.size()).isEqualTo(1);

    Notification n = ns.get(0);
    assertThat(n.getType()).isEqualByComparingTo(NotificationType.REPLY);
    assertThat(n.getSend().getId()).isEqualTo(5);
    assertThat(n.getTo().getId()).isEqualTo(1);
    assertThat(n.getTopic().getId()).isEqualTo(1);
    TimeAssert.assertNow(n.getCreateAt());
  }

  @WithUserDetails
  @Test
  void testUpdatePost() throws Exception {
    mockRequest.patch(mockMvc, "/api/post/1", jsonBody.getJson("/post/patchRawTopicBody.json"))
        .andExpect(status().isNoContent());

    List<Notification> ns = notificationRepository.findAll(Sort.by("id"));
    assertThat(ns.size()).isEqualTo(2);

    Notification n = ns.get(1);
    assertThat(n.getType()).isEqualByComparingTo(NotificationType.MENTION);
    assertThat(n.getSend().getId()).isEqualTo(1);
    assertThat(n.getTo().getId()).isEqualTo(4);
    assertThat(n.getTopic().getId()).isEqualTo(1);
    TimeAssert.assertNow(n.getCreateAt());
  }
}
