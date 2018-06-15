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

import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import java.time.Instant;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.OwnerCheck;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.check.Prefab;
import org.bicarb.core.forum.check.TopicFilterCheck;
import org.bicarb.core.forum.check.TopicPropertyCheck;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Topic Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"author", "lastReplyBy", "categories", "lockedBy", "deleteBy"})
@EqualsAndHashCode(of = "id")
@Setter
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "topics")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = Prefab.ROLE_NONE)
@ReadPermission(expression = PermissionCheck.TOPIC_DELETE
    + " OR (" + TopicFilterCheck.NON_DELETE
    + " OR " + PermissionCheck.TOPIC_DELETE_OWN
    + " AND " + TopicFilterCheck.DELETE_BY_SELF
    + ")")
@UpdatePermission(expression = Prefab.ROLE_NONE)
@SharePermission
public class Topic {

  private Integer id;
  private String title;
  private User author;
  private User lastReplyBy;
  private Instant lastReplyAt;  // if postIndex = 0, same as `createAt`
  private Integer postIndex;  // current index, zero-based, next index = +1
  private Set<Category> categories;

  private String slug;
  private Boolean locked;
  private Boolean delete;
  private Boolean pinned;
  private Boolean feature;

  private User lockedBy;  // last lock/unlock by
  private User deleteBy;  // last delete/restore by

  private Instant createAt;

  private String body;  // this only for create

  @Id
  @GeneratedValue(generator = "topic_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "topic_g", sequenceName = "topic_sequence", allocationSize = 5)
  public Integer getId() {
    return id;
  }

  @NotNull(message = "title should not be null")
  @Size(min = 1, max = 255, message = "title length should between 1 and 255")
  @Column(nullable = false)
  @CreatePermission(expression = PermissionCheck.TOPIC_CREATE)
  @UpdatePermission(expression = OwnerCheck.TOPIC_OWNER
      + " AND " + PermissionCheck.TOPIC_EDIT_OWN_TITLE
      + " OR " + PermissionCheck.TOPIC_EDIT_TITLE)
  public String getTitle() {
    return title;
  }

  @ManyToOne(optional = false)
  public User getAuthor() {
    return author;
  }

  @ManyToOne
  public User getLastReplyBy() {
    return lastReplyBy;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getLastReplyAt() {
    return lastReplyAt;
  }

  @Column(columnDefinition = "integer default 0", nullable = false)
  public Integer getPostIndex() {
    return postIndex;
  }

  @NotNull(message = "categories should not be null")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @ManyToMany
  @CreatePermission(expression = PermissionCheck.TOPIC_CREATE)
  @UpdatePermission(expression = PermissionCheck.CATEGORY_ALL) // CATEGORY_ALL: delete category.
  public Set<Category> getCategories() {
    return categories;
  }

  @Column(nullable = false)
  public String getSlug() {
    return slug;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = PermissionCheck.TOPIC_LOCKED
      + " OR " + OwnerCheck.TOPIC_OWNER
      + " AND " + PermissionCheck.TOPIC_LOCKED_OWN
      + " AND " + TopicPropertyCheck.NON_LOCKED_BY_OTHER)
  public Boolean getLocked() {
    return locked;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = PermissionCheck.TOPIC_DELETE
      + " OR " + OwnerCheck.TOPIC_OWNER
      + " AND " + PermissionCheck.TOPIC_DELETE_OWN
      + " AND " + TopicPropertyCheck.NON_DELETE_BY_OTHER)
  public Boolean getDelete() {
    return delete;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = PermissionCheck.TOPIC_PINNED)
  public Boolean getPinned() {
    return pinned;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = PermissionCheck.TOPIC_FEATURE)
  public Boolean getFeature() {
    return feature;
  }

  @ManyToOne
  public User getLockedBy() {
    return lockedBy;
  }

  @ManyToOne
  public User getDeleteBy() {
    return deleteBy;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getCreateAt() {
    return createAt;
  }

  @Transient
  @ComputedAttribute
  @CreatePermission(expression = PermissionCheck.TOPIC_CREATE)
  @ReadPermission(expression = Prefab.ROLE_NONE)
  public String getBody() {
    return body;
  }
}
