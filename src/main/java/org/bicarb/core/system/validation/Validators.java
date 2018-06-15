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

package org.bicarb.core.system.validation;

import com.yahoo.elide.core.exceptions.InvalidConstraintException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validators utils for elide hook function.
 *
 * @author olOwOlo
 */
@Component
public class Validators {

  private final Validator validator;

  @Autowired
  public Validators(Validator validator) {
    this.validator = validator;
  }

  public <T> void validate(T object) {
    Set<ConstraintViolation<T>> violations = validator.validate(object);
    if (!violations.isEmpty()) {
      throw new InvalidConstraintException(violations.iterator().next().getMessage());
    }
  }

  public <T> void validateProperty(T object, String propertyName) {
    Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName);
    if (!violations.isEmpty()) {
      throw new InvalidConstraintException(violations.iterator().next().getMessage());
    }
  }
}
