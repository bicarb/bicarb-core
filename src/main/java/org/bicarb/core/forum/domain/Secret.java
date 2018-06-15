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
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bicarb.core.forum.check.PermissionCheck;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Secret Entity.
 * Similar with {@link Setting}, but read permission is also required.
 * Some changes({@link org.bicarb.core.system.config.BicarbProperties})
 * will take effect after <strong>restart</strong> spring boot application.
 * TODO change this behaviour, or add a /api/config/reload endpoint.
 *
 * @author olOwOlo
 */
@EqualsAndHashCode(of = "key")
@ToString
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "secrets")
@Entity
@Include(rootLevel = true)
@CreatePermission(expression = PermissionCheck.SETTING_ALL)
@ReadPermission(expression = PermissionCheck.SETTING_ALL)
@UpdatePermission(expression = PermissionCheck.SETTING_ALL)
@DeletePermission(expression = PermissionCheck.SETTING_ALL)
public class Secret {

  private String key;
  private String value;

  @NotNull(message = "key should not be null")
  @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "Invalid key")
  @Id
  public String getKey() {
    return key;
  }

  @NotNull(message = "value should not be null")
  @Column(length = 65535)
  public String getValue() {
    return value;
  }
}
