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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.PermissionCheck;
import org.bicarb.core.forum.check.Prefab;

/**
 * Report Entity.
 *
 * @author olOwOlo
 */
@ToString(exclude = {"by", "post"})
@EqualsAndHashCode(of = "id")
@Setter
@NoArgsConstructor
@Table(name = "reports")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = Prefab.ROLE_NONE)
@ReadPermission(expression = PermissionCheck.REPORT_MANAGE)
@UpdatePermission(expression = Prefab.ROLE_NONE)
public class Report {

  private Integer id;
  private User by;
  private Post post;
  private String reason;
  private Instant handleAt;
  private Instant createAt;

  @Id
  @GeneratedValue(generator = "report_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "report_g", sequenceName = "report_sequence", allocationSize = 1)
  public Integer getId() {
    return id;
  }

  @ManyToOne(optional = false)
  public User getBy() {
    return by;
  }

  @NotNull(message = "post should not be null")
  @ManyToOne(optional = false)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  public Post getPost() {
    return post;
  }

  @NotNull(message = "reason should not be null")
  @Column(nullable = false)
  @CreatePermission(expression = Prefab.ROLE_ALL)
  public String getReason() {
    return reason;
  }

  public Instant getHandleAt() {
    return handleAt;
  }

  @Column(columnDefinition = "timestamp default now()", nullable = false)
  public Instant getCreateAt() {
    return createAt;
  }
}
