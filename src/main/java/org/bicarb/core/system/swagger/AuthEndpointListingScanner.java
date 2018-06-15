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

package org.bicarb.core.system.swagger;

import com.fasterxml.classmate.TypeResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;

/**
 * Manual define /login and /logout.
 *
 * @author olOwOlo
 */
@Component
public class AuthEndpointListingScanner implements ApiListingScannerPlugin {

  private final CachingOperationNameGenerator operationNames;

  @Autowired
  public AuthEndpointListingScanner(CachingOperationNameGenerator operationNames) {
    this.operationNames = operationNames;
  }

  @Override
  public List<ApiDescription> apply(DocumentationContext documentationContext) {
    return new ArrayList<>(List.of(
        new ApiDescription(
            "/login",
            "login",
            List.of(new OperationBuilder(operationNames)
                .method(HttpMethod.POST)
                .consumes(Set.of(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .codegenMethodNameStem("manualDefineLoginPost")
                .summary("login")
                .parameters(List.of(
                    new ParameterBuilder()
                        .name("username")
                        .description("username or email")
                        .type(new TypeResolver().resolve(String.class))
                        .parameterType("query")
                        .required(true)
                        .modelRef(new ModelRef("string"))
                        .build(),
                    new ParameterBuilder()
                        .name("password")
                        .description("password")
                        .type(new TypeResolver().resolve(String.class))
                        .parameterType("query")
                        .required(true)
                        .modelRef(new ModelRef("string"))
                        .build(),
                    new ParameterBuilder()
                        .name("remember-me")
                        .description("true or false, remember 60 days")
                        .type(new TypeResolver().resolve(Boolean.class))
                        .parameterType("query")
                        .required(false)
                        .modelRef(new ModelRef("boolean"))
                        .build()
                ))
                .responseMessages(Set.of(
                    new ResponseMessageBuilder()
                        .code(200)
                        .message("login success")
                        .build(),
                    new ResponseMessageBuilder()
                        .code(401)
                        .message("login fail")
                        .build()
                ))
                .responseModel(new ModelRef("string"))
                .build()),
            false),
        new ApiDescription(
            "/logout",
            "logout",
            List.of(new OperationBuilder(operationNames)
                .method(HttpMethod.POST)
                .codegenMethodNameStem("manualDefineLogoutGet")
                .summary("logout")
                .responseMessages(Set.of(
                    new ResponseMessageBuilder()
                        .code(302)
                        .message("Redirect to root page '/'")
                        .build()
                ))
                .responseModel(new ModelRef("string"))
                .build()),
            false)
    ));
  }

  @Override
  public boolean supports(DocumentationType documentationType) {
    return DocumentationType.SWAGGER_2.equals(documentationType);
  }
}
