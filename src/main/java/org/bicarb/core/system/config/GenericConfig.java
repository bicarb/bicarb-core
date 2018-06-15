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

import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bicarb.core.forum.domain.Secret;
import org.bicarb.core.forum.repository.SecretRepository;
import org.bicarb.core.system.config.BicarbProperties.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Generic Config.
 *
 * @author olOwOlo
 */
@EnableCaching
@EnableAsync
@EnableScheduling
@EntityScan({"org.bicarb.core..*.domain", "org.bicarb.core.system.security"})
@Configuration
public class GenericConfig {

  private final SecretRepository secretRepository;

  @Autowired
  public GenericConfig(SecretRepository secretRepository) {
    this.secretRepository = secretRepository;
  }

  @Bean
  public RequestContextListener requestContextListener() {
    return new RequestContextListener();
  }

  /** Async task executor. */
  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(2);
    taskExecutor.setMaxPoolSize(5);
    taskExecutor.setQueueCapacity(100);
    return taskExecutor;
  }

  @Bean
  public TaskScheduler taskScheduler() {
    return new ThreadPoolTaskScheduler();
  }

  /**
   * BicarbProperties.
   */
  @Bean
  public BicarbProperties bicarbProperties() {
    Map<String, Secret> secrets =
        secretRepository.findAll().stream().collect(Collectors.toMap(Secret::getKey, s -> s));

    return BicarbProperties.builder()
        .url(getSecretOrNull("url", secrets))
        .userLinkFormat(getSecretOrNull("userLinkFormat", secrets))
        .mail(Mail.builder()
            .host(getSecretOrNull("mail.host", secrets))
            .username(getSecretOrNull("mail.username", secrets))
            .password(getSecretOrNull("mail.password", secrets))
            .address(getSecretOrNull("mail.address", secrets))
            .personal(getSecretOrNull("mail.personal", secrets))
            .build())
        .build();
  }

  @Nullable
  private String getSecretOrNull(String id, Map<String, Secret> secrets) {
    Secret s = secrets.get(id);
    return s == null ? null : s.getValue();
  }
}
