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

package org.bicarb.core.forum.controller;

import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.service.SeoViewService;
import org.bicarb.core.system.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * SeoViewController.
 *
 * @author olOwOlo
 */
@Controller
public class SeoViewController {

  private final SeoViewService seoViewService;

  @Autowired
  public SeoViewController(SeoViewService seoViewService) {
    this.seoViewService = seoViewService;
  }

  /**
   * Put Content-Security-Policy nonce into model.
   * Add Content-Security-Policy header if there is a policy string.
   */
  @ModelAttribute("nonce")
  public String nonce(HttpServletResponse response) {
    Pair<String, Optional<String>> o = seoViewService.getNonceAndCsp();
    o.getRight().ifPresent(policy -> response.addHeader(Constant.CSP, policy));
    return o.getLeft();
  }

  @GetMapping("/")
  public ModelAndView index(@RequestParam(required = false) Integer page, Principal auth) {

    return new ModelAndView("index", seoViewService.topicListViewModel(page, auth));
  }

  @GetMapping("/topic/{topicId}/**")
  public ModelAndView topicPage(
      @PathVariable String topicId, @RequestParam(required = false) Integer page, Principal auth) {

    return new ModelAndView("index", StringUtils.isNumeric(topicId)
        ? seoViewService.topicViewModel(Integer.parseInt(topicId), page, auth)
        : seoViewService.getCommonModel(auth));
  }

  @GetMapping("/**/{nonDot:[^.]+}")
  public ModelAndView anyOther(Principal auth) {

    return new ModelAndView("index", seoViewService.getCommonModel(auth));
  }
}
