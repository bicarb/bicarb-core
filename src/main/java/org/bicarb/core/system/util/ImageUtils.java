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

package org.bicarb.core.system.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.util.ThumbnailatorUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ImageUtils.
 *
 * @author olOwOlo
 */
public class ImageUtils {

  private static final HashFunction MURMUR3_128 = Hashing.murmur3_128();
  private static final float OUTPUT_QUALITY = 0.75f;

  /**
   * Save image with name based on hashcode in hierarchical directory and return relative path.
   * e.g. e.g. image.png -> [a-zA-Z0-9]{dirLength}/rest_hash.png
   *
   * @param bytes image data
   * @param relativeDir relative directory path, e.g. "upload", "upload/image", "upload/img/avatar"
   * @param extension image extension, e.g. "jpg", "png"
   * @param dirLength how many character are used to form a hierarchical directory
   * @return image relative path, excluding relativeDir
   * @throws IOException IOException
   */
  public static String saveImage(byte[] bytes, String relativeDir, String extension, int dirLength)
      throws IOException {
    String hashString = MURMUR3_128.hashBytes(bytes).toString();
    String dir = hashString.substring(0, dirLength);
    String rest = hashString.substring(dirLength);
    Path filePath = Paths.get(dir, rest + "." + extension);
    Path fullPath = Paths.get(relativeDir).resolve(filePath);
    Path dirPath = fullPath.getParent();

    if (!Files.exists(dirPath)) {
      Files.createDirectories(dirPath);
    }
    Files.write(fullPath, bytes);

    return filePath.toString();
  }

  /**
   * Resize avatar(Now, animated gif is not supported).
   *
   * @param is InputStream
   * @param os OutputStream
   * @param extension One of [JPG, jpg, tiff, bmp, BMP, gif, GIF,
   *     WBMP, png, PNG, JPEG, tif, TIF, TIFF, jpeg, wbmp]
   * @throws IOException IOException
   */
  public static void resizeAvatar(InputStream is, OutputStream os, String extension)
      throws IOException {
    Thumbnails.of(is)
        .size(150, 150)
        .outputQuality(OUTPUT_QUALITY)
        .imageType(BufferedImage.TYPE_INT_ARGB)
        .outputFormat(extension)
        .toOutputStream(os);
  }

  /**
   * Resize(optional) & compress common image.
   *
   * <b>NOTE: </b> The 'gif'/'GIF' format is unsupported.
   *
   * @param is InputStream
   * @param os OutputStream
   * @param extension One of [JPG, jpg, tiff, bmp, BMP, gif, GIF,
   *     WBMP, png, PNG, JPEG, tif, TIF, TIFF, jpeg, wbmp]
   * @throws IOException IOException
   * @deprecated Size may increase after compression.
   */
  @Deprecated
  public static void resize(InputStream is, OutputStream os, String extension)
      throws IOException {
    if (isGif(extension)) {
      throw new UnsupportedOperationException("gif is not support");
    }

    BufferedImage image = ImageIO.read(is);

    Builder<BufferedImage> builder = Thumbnails.of(image)
        .outputQuality(OUTPUT_QUALITY)
        .imageType(BufferedImage.TYPE_INT_ARGB)
        .outputFormat(extension);

    if (image.getWidth() > 2560 || image.getHeight() > 1440) {
      builder.size(2560, 1440);
    } else {
      builder.scale(1d);
    }

    builder.toOutputStream(os);
  }

  /**
   * isSupportedExtension.
   * @param extension extension string
   * @return {@literal true} if in [JPG, jpg, tiff, bmp, BMP, gif, GIF,
   *     WBMP, png, PNG, JPEG, tif, TIF, TIFF, jpeg, wbmp]
   */
  public static boolean isSupportedExtension(String extension) {
    Objects.requireNonNull(extension, "extension should not be null");
    return ThumbnailatorUtils.isSupportedOutputFormat(extension);
  }

  /**
   * isGif.
   * @param extension extension string
   * @return boolean
   */
  public static boolean isGif(String extension) {
    return "gif".equals(StringUtils.lowerCase(extension));
  }
}
