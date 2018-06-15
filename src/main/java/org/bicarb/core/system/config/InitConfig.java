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

import com.yahoo.elide.utils.coerce.CoerceUtil;
import javax.annotation.PostConstruct;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * All PostConstruct Init.
 *
 * @author olOwOlo
 */
@Configuration
public class InitConfig {

  private static final Logger logger = LoggerFactory.getLogger(InitConfig.class);

  @PostConstruct
  public void init() {
    registerConverterForElide();
  }

  @SuppressWarnings("unchecked")
  private void registerConverterForElide() {
    CoerceUtil.coerce("", String.class);
    ConvertUtils.register(new Converter() {
      @Override
      public <T> T convert(Class<T> type, Object value) {
        if (value instanceof String) {
          return (T) java.time.Instant.parse(String.valueOf(value));
        } else if (value instanceof Number) {
          return (T) java.time.Instant.ofEpochMilli(((Number) value).longValue());
        } else {
          throw new ConversionException("Could not perform convert for [java.time.Instant]");
        }
      }
    }, java.time.Instant.class);
    logger.info("ConvertUtils.register(converter, java.time.Instant.class)");
  }
}
