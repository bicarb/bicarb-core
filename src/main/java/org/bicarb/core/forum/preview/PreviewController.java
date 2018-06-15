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

package org.bicarb.core.forum.preview;

import javax.validation.Valid;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.service.PostService;
import org.bicarb.core.system.bean.Renderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PreviewController.
 *
 * @author olOwOlo
 */
@RequestMapping("/api")
@RestController
public class PreviewController {

  private final Renderer renderer;
  private final PostService postService;

  @Autowired
  public PreviewController(Renderer renderer, PostService postService) {
    this.renderer = renderer;
    this.postService = postService;
  }

  /**
   * [POST][/preview].
   * Preview markdown render.
   */
  @PostMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
  public PreviewDto preview(@RequestBody @Valid PreviewDto dto) {

    String cooked = dto.getTopicBody() == null || dto.getTopicBody().equals(false)
        ? renderer.renderPost(dto.getBody())
        : renderer.renderTopic(dto.getBody());

    Post p = Post.builder().cooked(cooked).build();
    postService.getMentionedList(p, null);

    dto.setBody(p.getCooked());
    return dto;
  }
}
