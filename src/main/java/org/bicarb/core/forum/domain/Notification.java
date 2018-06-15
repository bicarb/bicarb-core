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

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.NotificationFilterCheck;
import org.bicarb.core.forum.check.OwnerCheck;
import org.bicarb.core.forum.check.Prefab;

/**
 * Notification Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"send", "to", "post", "topic"})
@EqualsAndHashCode(of = "id")
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notifications")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = Prefab.ROLE_NONE)
@ReadPermission(expression = NotificationFilterCheck.OWNER)
@UpdatePermission(expression = Prefab.ROLE_NONE)
public class Notification {

  private Integer id;
  private NotificationType type;
  private User send;
  private User to;
  private Post post;
  private Topic topic;

  private Instant readAt;
  private Instant createAt;

  public enum NotificationType {
    REPLY,
    MENTION
  }

  @Id
  @GeneratedValue(generator = "notification_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "notification_g",
      sequenceName = "notification_sequence", allocationSize = 5)
  public Integer getId() {
    return id;
  }

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public NotificationType getType() {
    return type;
  }

  @ManyToOne(optional = false)
  public User getSend() {
    return send;
  }

  @ManyToOne(optional = false)
  public User getTo() {
    return to;
  }

  @ManyToOne(optional = false)
  public Post getPost() {
    return post;
  }

  @ManyToOne(optional = false)
  public Topic getTopic() {
    return topic;
  }

  @UpdatePermission(expression = OwnerCheck.NOTIFICATION_OWNER)
  public Instant getReadAt() {
    return readAt;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getCreateAt() {
    return createAt;
  }
}
