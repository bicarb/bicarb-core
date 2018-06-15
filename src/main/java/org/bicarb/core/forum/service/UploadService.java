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

package org.bicarb.core.forum.service;

import static org.bicarb.core.system.Constant.UPLOAD_AVATAR_DIR;
import static org.bicarb.core.system.Constant.UPLOAD_IMAGE_DIR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.bicarb.core.system.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadService.
 *
 * @author olOwOlo
 */
@Service
public class UploadService {

  private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

  private final UserRepository userRepository;

  @Autowired
  public UploadService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * save image.
   *
   * @see UploadService#save(MultipartFile, boolean)
   */
  public Optional<String> saveImage(MultipartFile file) throws IOException {
    return save(file, false);
  }

  /**
   * save image & update user avatar.
   *
   * @see UploadService#save(MultipartFile, boolean)
   */
  @Transactional
  public Optional<String> saveAvatar(MultipartFile file, Principal auth) throws IOException {
    return save(file, true)
        .map(p -> {
          User u = userRepository.getOne(AuthenticationUtils.fetchUserId(auth));
          u.setAvatar(p);
          return Optional.of(p);
        }).orElseGet(Optional::empty);
  }

  /**
   * Resize(avatar?) & compress(avatar?) & save & hash & return file path.
   * e.g. image.png -> [a-zA-Z0-9]{2}/rest_hash.png
   *
   * @param file MultipartFile
   * @param avatar if true, resize to 150x150
   * @return Optional.empty() if extension is unsupported.
   * @throws IOException IOException
   * @see ImageUtils#saveImage(byte[], String, String, int)
   * @see ImageUtils#resizeAvatar(InputStream, OutputStream, String)
   */
  private Optional<String> save(MultipartFile file, boolean avatar) throws IOException {
    String extension = getExtension(file);

    logger.debug("upload file: '{}', Content-Type: '{}', extension: '{}'",
        file.getOriginalFilename(), file.getContentType(), extension);

    if (!ImageUtils.isSupportedExtension(extension)) {
      return Optional.empty();
    }

    byte[] imageData;
    if (avatar) {
      try (InputStream is = file.getInputStream();
          ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        ImageUtils.resizeAvatar(is, os, extension);
        imageData = os.toByteArray();
      }
    } else {
      imageData = file.getBytes();
    }

    logger.debug("file size: before[{}] after[{}]", file.getSize(), imageData.length);

    String imgPath =  ImageUtils.saveImage(imageData,
        avatar ? UPLOAD_AVATAR_DIR : UPLOAD_IMAGE_DIR, extension, 2);
    return Optional.of(imgPath);
  }

  private final Pattern pattern = Pattern.compile("^\\S*/(\\S*)$");

  /**
   * If extension not found, fallback to 'png'.
   */
  private String getExtension(MultipartFile file) {
    String of = file.getOriginalFilename();
    String ct = file.getContentType();

    return of != null && !of.equals("")
        ? com.google.common.io.Files.getFileExtension(of)
        : (ct != null
            ? getExtensionFromContentType(ct)
            : "png");
  }

  private String getExtensionFromContentType(String contentType) {
    Matcher matcher = pattern.matcher(contentType);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      throw new IllegalStateException("input should be a valid Content-Type");
    }
  }
}
