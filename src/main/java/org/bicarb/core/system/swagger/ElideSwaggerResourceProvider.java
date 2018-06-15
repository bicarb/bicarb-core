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

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;

/**
 * ElideSwaggerResourceProvider.
 *
 * @author olOwOlo
 */
@Primary
@Component
public class ElideSwaggerResourceProvider extends InMemorySwaggerResourcesProvider {

  public ElideSwaggerResourceProvider(Environment environment,
      DocumentationCache documentationCache) {
    super(environment, documentationCache);
  }

  @Override
  public List<SwaggerResource> get() {
    final List<SwaggerResource> srs = super.get();

    SwaggerResource sr = new SwaggerResource();
    sr.setName("Elide");
    sr.setLocation("/v2/api-docs/elide");
    sr.setSwaggerVersion("2.0");
    srs.add(sr);

    return srs;
  }
}
