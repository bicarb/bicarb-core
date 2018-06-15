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

package org.bicarb.core.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.bicarb.core.BaseSetup;
import org.bicarb.core.system.config.BicarbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * PropertiesTest.
 *
 * @author olOwOlo
 */
public class PropertiesTest extends BaseSetup {

  @Autowired
  private BicarbProperties bicarbProperties;

  @Test
  void testProperties() {
    assertThat(bicarbProperties.getUrl()).isEqualTo("http://localhost");
    assertThat(bicarbProperties.getUserLinkFormat()).isEqualTo("<a class=\"user-link\" href=\"/user/%s\">@%s </a>");
    BicarbProperties.Mail mail = bicarbProperties.getMail();
    assertThat(mail.getHost()).isNotNull();
    assertThat(mail.getUsername()).isNotNull();
    assertThat(mail.getPassword()).isNotNull();
    assertThat(mail.getAddress()).isNotNull();
    assertThat(mail.getPersonal()).isEqualTo("Bicarb");
  }
}
