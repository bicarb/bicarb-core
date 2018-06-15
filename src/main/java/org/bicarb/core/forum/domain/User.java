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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.OwnerCheck;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.check.Prefab;
import org.bicarb.core.forum.check.UserPropertyCheck;
import org.bicarb.core.system.validation.ValidEmail;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * User Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"group", "password"})
@EqualsAndHashCode(of = "id")
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "users")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = Prefab.ROLE_NONE)
@UpdatePermission(expression = Prefab.ROLE_NONE)
public class User {

  private Integer id;
  private String username;
  private String nickname;
  private String password;
  private String email;
  private Boolean emailPublic;
  private String avatar;
  private String bio;
  private String website;
  private String github;

  private Integer topicCount;
  private Integer postCount; // Do not include post 0.

  private Boolean active;
  private Instant lockedAt;
  private Instant lockedUntil;

  private Instant lastSignInAt;
  private String lastSignIp;
  private Instant createAt;

  private Group group;

  @Id
  @GeneratedValue(generator = "user_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "user_g", sequenceName = "user_sequence", allocationSize = 5)
  public Integer getId() {
    return id;
  }

  @NotNull(message = "username should not be null")
  @Pattern(message = "Invalid username", regexp = "^\\w{1,30}$")
  @Column(unique = true, nullable = false, length = 30)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  public String getUsername() {
    return username;
  }

  @NotNull(message = "nickname should not be null")
  @Pattern(message = "Invalid nickname", regexp = "^\\S{1,30}$")
  @Column(unique = true, nullable = false, length = 30)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  public String getNickname() {
    return nickname;
  }

  @NotNull(message = "password should not be null")
  @Column(nullable = false)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  @ReadPermission(expression = Prefab.ROLE_NONE)
  public String getPassword() {
    return password;
  }

  @ValidEmail
  @Column(unique = true, nullable = false)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  @ReadPermission(expression = UserPropertyCheck.EMAIL_PUBLIC + " OR " + OwnerCheck.USER_OWNER)
  public String getEmail() {
    return email;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  @UpdatePermission(expression = OwnerCheck.USER_OWNER)
  public Boolean getEmailPublic() {
    return emailPublic;
  }

  public String getAvatar() {
    return avatar;
  }

  @UpdatePermission(expression = OwnerCheck.USER_OWNER)
  public String getBio() {
    return bio;
  }

  @Pattern(regexp = "^[a-zA-Z0-9.%=&?:/_-]+$", message = "Invalid website")
  @UpdatePermission(expression = OwnerCheck.USER_OWNER)
  public String getWebsite() {
    return website;
  }

  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid github name")
  @UpdatePermission(expression = OwnerCheck.USER_OWNER)
  public String getGithub() {
    return github;
  }

  @Column(columnDefinition = "integer default 0", nullable = false)
  public Integer getTopicCount() {
    return topicCount;
  }

  @Column(columnDefinition = "integer default 0", nullable = false)
  public Integer getPostCount() {
    return postCount;
  }

  @Column(columnDefinition = "boolean default false", nullable = false)
  public Boolean getActive() {
    return active;
  }

  public Instant getLockedAt() {
    return lockedAt;
  }

  @UpdatePermission(expression = PermissionCheck.USER_LOCK)
  public Instant getLockedUntil() {
    return lockedUntil;
  }

  public Instant getLastSignInAt() {
    return lastSignInAt;
  }

  @ReadPermission(expression = PermissionCheck.IP_READ)
  public String getLastSignIp() {
    return lastSignIp;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getCreateAt() {
    return createAt;
  }

  // `columnDefinition` not work unless there is an explicit name.
  // Default 3 is user group.
  @JoinColumn(name = "group_id", columnDefinition = "integer default 3")
  @ManyToOne(optional = false)
  @UpdatePermission(expression = PermissionCheck.GROUP_ALL)
  public Group getGroup() {
    return group;
  }
}
