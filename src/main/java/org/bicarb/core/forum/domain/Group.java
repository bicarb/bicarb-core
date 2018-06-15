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
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.PermissionCheck;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Group Entity.
 *
 * @author olOwOlo
 */
@ToString
@EqualsAndHashCode(of = "id")
@Setter
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "groups")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = PermissionCheck.GROUP_ALL)
@UpdatePermission(expression = PermissionCheck.GROUP_ALL)
@DeletePermission(expression = PermissionCheck.GROUP_ALL)
@SharePermission
public class Group {

  private Integer id;
  private String name;
  private String color;
  private String icon;
  private Set<String> permissions;

  @Id
  @GeneratedValue(generator = "group_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "group_g", sequenceName = "group_sequence", allocationSize = 1)
  public Integer getId() {
    return id;
  }

  @NotNull(message = "name should not be null")
  @Size(min = 1, max = 20, message = "name length should between 1 and 20")
  @Column(unique = true, nullable = false, length = 20)
  public String getName() {
    return name;
  }

  @Pattern(regexp = "^#?(?:[a-fA-F0-9]{3}){1,2}$", message = "Invalid color")
  public String getColor() {
    return color;
  }

  @Pattern(regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)+$", message = "Invalid icon")
  public String getIcon() {
    return icon;
  }

  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @ElementCollection
  public Set<String> getPermissions() {
    return permissions;
  }
}
