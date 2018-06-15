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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author olOwOlo
 */
@Component
public class JsonBody {

  @Autowired
  private ObjectMapper objectMapper;

  public String getJson(String filePath) {
    try (InputStream is = JsonBody.class.getResourceAsStream(filePath)) {
      return String.valueOf(objectMapper.readTree(is));
    } catch (IOException e) {
      Assertions.fail("Unable to open json file [" + filePath + "].", e);
      return null;  // appeasing the compiler: this line will never be executed.
    }
  }
}
