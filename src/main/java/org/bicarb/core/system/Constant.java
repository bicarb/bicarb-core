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

/**
 * Constant.
 *
 * @author olOwOlo
 */
public class Constant {

  public static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

  public static final String CSP = "Content-Security-Policy";

  public static final String STATIC_LOCATION = "public/";
  public static final String UPLOAD_DIR = STATIC_LOCATION + "upload";
  public static final String UPLOAD_IMAGE_DIR = UPLOAD_DIR + "/image";
  public static final String UPLOAD_AVATAR_DIR = UPLOAD_DIR + "/avatar";

  public static final String QUERY_CACHE_HINT = "org.hibernate.cacheable";
}
