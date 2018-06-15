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

package org.bicarb.core.system.bean;

import java.security.SecureRandom;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SecureRandoms.
 *
 * @author olOwOlo
 */
@Component
public class SecureRandoms {

  @Getter
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * generate random bytes.
   * @param length byte length
   * @return random bytes
   */
  public byte[] generateBytes(int length) {
    byte[] bytes = new byte[length];
    secureRandom.nextBytes(bytes);
    return bytes;
  }

  /**
   * resetSeed.
   */
  public void resetSeed() {
    secureRandom.setSeed(secureRandom.generateSeed(16));
  }

  /**
   * Reset seed regularly.
   */
  @Scheduled(fixedDelay = 1000 * 60 * 60)
  public void schedule() {
    if (secureRandom.nextBoolean()) {
      resetSeed();
    }
  }
}
