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

package org.bicarb.core.forum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bicarb.core.system.Constant.UPLOAD_AVATAR_DIR;
import static org.bicarb.core.system.Constant.UPLOAD_DIR;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.forum.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

/**
 * @author olOwOlo
 */
public class UploadTest extends BaseSetup {

  private static final Logger logger = LoggerFactory.getLogger(UploadTest.class);

  private static final String COMMON_PATH_REGEX = "[a-z0-9]{2}[/\\\\][a-z0-9]+\\.";

  @Autowired
  private UserRepository userRepository;

  @AfterAll
  static void afterAll() throws IOException {
    Path path = Paths.get(UPLOAD_DIR);
    logger.info("Delete [{}] after all tests.", path.toAbsolutePath());
    FileSystemUtils.deleteRecursively(path);
  }

  @Test
  void testUpload() throws Exception {
    MockHttpServletResponse response = mockRequest.multipart(mockMvc, "/api/upload",
        getFile("test.gif", "image/gif", "/image/test.gif"))
        .andExpect(status().isCreated())
        .andReturn().getResponse();

    assertThat(response.getContentAsString()).matches(COMMON_PATH_REGEX + "gif");
  }

  @Test
  void testUploadAvatar() throws Exception {
    MockHttpServletResponse response = mockRequest.multipart(mockMvc, "/api/upload/avatar",
        getFile("test.gif", "image/gif", "/image/test.gif"))
        .andExpect(status().isCreated())
        .andReturn().getResponse();

    assertThat(response.getContentAsString()).matches(COMMON_PATH_REGEX + "gif");
    assertUserAvatar("gif");
  }

  @Test
  void testUploadInvalidFormat() throws Exception {
    mockRequest.multipart(mockMvc, "/api/upload/avatar",
        getFile(null, "application/json", "/user/post.json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUnknownFormat() throws Exception {
    MockHttpServletResponse response = mockRequest.multipart(mockMvc, "/api/upload/avatar",
        getFile(null, null, "/image/unknown.jpg"))
        .andExpect(status().isCreated())
        .andReturn().getResponse();

    assertThat(response.getContentAsString()).matches(COMMON_PATH_REGEX + "png");
    assertUserAvatar("png");
  }

  private void assertUserAvatar(String extension) throws IOException {
    String avatar = userRepository.getOne(1).getAvatar();
    assertThat(avatar).matches(COMMON_PATH_REGEX + extension);

    BufferedImage image = ImageIO.read(new File(UPLOAD_AVATAR_DIR + "/" + avatar));
    assertThat(image.getHeight()).isLessThanOrEqualTo(150);
    assertThat(image.getWidth()).isLessThanOrEqualTo(150);
  }

  private MockMultipartFile getFile(
      @Nullable String fileName, @Nullable String contentType, String path) throws IOException {

    return new MockMultipartFile("file", fileName, contentType,
        UploadTest.class.getResourceAsStream(path));
  }
}
