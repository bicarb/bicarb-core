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
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
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
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.check.Prefab;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Category Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"parent"})
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "categories", indexes = {
    @Index(name = "uk_category_parent_position", columnList = "parent_id, position", unique = true)
})
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = PermissionCheck.CATEGORY_ALL)
@UpdatePermission(expression = PermissionCheck.CATEGORY_ALL)
@DeletePermission(expression = PermissionCheck.CATEGORY_ALL)
@SharePermission
public class Category {

  private Integer id;
  private String slug;
  private String name;
  private String description;
  private Integer topicCount;
  private Integer position;

  private Category parent;

  @Id
  @GeneratedValue(generator = "category_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "category_g", sequenceName = "category_sequence", allocationSize = 1)
  public Integer getId() {
    return id;
  }

  @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "slug regex expression is '^[A-Za-z0-9_-]+$'")
  @Column(nullable = false, unique = true)
  public String getSlug() {
    return slug;
  }

  @NotNull(message = "name should not be null")
  @Column(nullable = false)
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Column(columnDefinition = "integer default 0", nullable = false)
  @UpdatePermission(expression = Prefab.ROLE_NONE)
  public Integer getTopicCount() {
    return topicCount;
  }

  @Column(nullable = false)
  @UpdatePermission(expression = Prefab.ROLE_NONE)
  public Integer getPosition() {
    return position;
  }

  @ManyToOne
  @UpdatePermission(expression = Prefab.ROLE_NONE)
  public Category getParent() {
    return parent;
  }
}
