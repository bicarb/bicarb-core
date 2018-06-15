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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.bicarb.core.system.Constant;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * MockMvcRequestBuilders Utils.
 *
 * @author olOwOlo
 */
@Component
public class MockRequest {

  public static final String JSON_API_CONTENT_TYPE = Constant.JSON_API_CONTENT_TYPE;
  // TODO remove parameter
  public static final String JSON_API_RESPONSE = JSON_API_CONTENT_TYPE + ";charset=UTF-8";

  public ResultActions get(MockMvc mockMvc, String path) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.get(path)
        .accept(JSON_API_RESPONSE));
  }

  public ResultActions post(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.post(path)
        .with(csrf().asHeader())
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(body));
  }

  public ResultActions patch(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.patch(path)
        .with(csrf().asHeader())
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(body));
  }

  public ResultActions delete(MockMvc mockMvc, String path) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.delete(path)
        .with(csrf().asHeader())
        .accept(JSON_API_RESPONSE));
  }

  public ResultActions delete(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.delete(path)
        .with(csrf().asHeader())
        .accept(JSON_API_RESPONSE)
        .contentType(JSON_API_CONTENT_TYPE)
        .content(body));
  }

  public ResultActions patchForm(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.patch(path)
        .with(csrf().asHeader())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .content(body));
  }

  public ResultActions postForm(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.post(path)
        .with(csrf().asHeader())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .content(body));
  }

  public ResultActions postJson(MockMvc mockMvc, String path, String body) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.post(path)
        .with(csrf().asHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(body));
  }

  public ResultActions postNoBody(MockMvc mockMvc, String path) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.post(path)
        .with(csrf().asHeader()));
  }

  public ResultActions patchNoBody(MockMvc mockMvc, String path) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.patch(path)
        .with(csrf().asHeader()));
  }

  public ResultActions multipart(MockMvc mockMvc, String path, MockMultipartFile file) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders.multipart(path)
        .file(file)
        .with(csrf().asHeader()));
  }
}
