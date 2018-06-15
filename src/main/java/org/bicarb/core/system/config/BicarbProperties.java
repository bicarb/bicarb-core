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

package org.bicarb.core.system.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bicarb properties.
 *
 * @see org.bicarb.core.forum.domain.Secret
 * @author olOwOlo
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BicarbProperties {

  /**
   * Used in mails, last character should not be '/'.
   */
  private String url;
  private String userLinkFormat;

  private Mail mail;

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Mail {
    private String host;
    private String username;
    private String password;
    private String address;
    private String personal;
  }
}
