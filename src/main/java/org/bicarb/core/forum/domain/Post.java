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
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.OwnerCheck;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.check.PostFilterCheck;
import org.bicarb.core.forum.check.PostPropertyCheck;
import org.bicarb.core.forum.check.Prefab;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Post Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"topic", "author"})
@EqualsAndHashCode(of = "id")
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "posts", indexes = {
    @Index(name = "uk_topic_id_index", columnList = "topic_id, index", unique = true)
})
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = Prefab.ROLE_NONE)
@ReadPermission(expression = PermissionCheck.POST_DELETE
    + " OR (" + PostFilterCheck.TOPIC_NON_DELETE
    + " AND (" + PostFilterCheck.NON_DELETE
    + " OR " + PermissionCheck.POST_DELETE_OWN
    + " AND " + PostFilterCheck.DELETE_BY_SELF
    + "))")
@UpdatePermission(expression = Prefab.ROLE_NONE)
@SharePermission
public class Post {

  private Integer id;
  private String raw;
  private String cooked;
  private Topic topic;
  private User author;
  private Integer index;

  private Instant lastEditAt;
  private Boolean delete;
  private User deleteBy;

  private String ip;
  private Instant createAt;

  @Id
  @GeneratedValue(generator = "post_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "post_g", sequenceName = "post_sequence", allocationSize = 5)
  public Integer getId() {
    return id;
  }

  @NotNull(message = "raw should not be null")
  @Column(columnDefinition = "text", nullable = false)
  @CreatePermission(expression = PermissionCheck.POST_CREATE)
  @UpdatePermission(expression = PermissionCheck.POST_EDIT_OWN_CONTENT
      + " AND " + OwnerCheck.POST_OWNER
      + " OR " + PermissionCheck.POST_EDIT_CONTENT)
  public String getRaw() {
    return raw;
  }

  @Column(columnDefinition = "text", nullable = false)
  public String getCooked() {
    return cooked;
  }

  @NotNull(message = "topic should not be null")
  @ManyToOne(optional = false)
  @CreatePermission(expression = PermissionCheck.POST_CREATE
      + " AND " + PostPropertyCheck.TOPIC_NON_DELETE
      + " AND " + PostPropertyCheck.TOPIC_NON_LOCKED)
  public Topic getTopic() {
    return topic;
  }

  @ManyToOne(optional = false)
  public User getAuthor() {
    return author;
  }

  @Column(nullable = false)
  public Integer getIndex() {
    return index;
  }

  public Instant getLastEditAt() {
    return lastEditAt;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = PermissionCheck.POST_DELETE
      + " OR " + OwnerCheck.POST_OWNER
      + " AND " + PermissionCheck.POST_DELETE_OWN
      + " AND " + PostPropertyCheck.NON_DELETE_BY_OTHER)
  public Boolean getDelete() {
    return delete;
  }

  @ManyToOne
  public User getDeleteBy() {
    return deleteBy;
  }

  @Column(nullable = false)
  @ReadPermission(expression = PermissionCheck.IP_READ)
  public String getIp() {
    return ip;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getCreateAt() {
    return createAt;
  }
}
