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

package org.bicarb.core;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author olOwOlo
 */
@WithUserDetails(userDetailsServiceBeanName = "bicarbUserDetailsService")
@Transactional
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BaseSetup {

  @Autowired
  protected MockRequest mockRequest;
  @Autowired
  protected JsonBody jsonBody;
  @Autowired
  protected WebApplicationContext wac;

  protected MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

}