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

import com.google.common.collect.ImmutableList;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.options.MutableDataSet;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Renderer Markdown.
 *
 * @author olOwOlo
 */
@Component
public class Renderer {

  private static MutableDataSet getCommonOptions() {
    return new MutableDataSet()
        .set(HtmlRenderer.SOFT_BREAK, "<br />\n")
        .set(HtmlRenderer.ESCAPE_HTML, true)
        .set(HtmlRenderer.INDENT_SIZE, 2)
        .set(HtmlRenderer.GENERATE_HEADER_ID, true)
        .set(AdmonitionExtension.CONTENT_INDENT, 2)
        .set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false)
        .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor-link")
        .set(EmojiExtension.ROOT_IMAGE_PATH, "/images/emoji/")
        .set(EmojiExtension.ATTR_IMAGE_CLASS, "emoji-fallback-image")
        .set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.UNICODE_FALLBACK_TO_IMAGE);
  }

  private static final List<Extension> POST_EXTENSIONS = ImmutableList.of(
      AutolinkExtension.create(),   // current implementation, performance impact, large files
      EmojiExtension.create(),
      StrikethroughExtension.create(),
      TaskListExtension.create(),
      InsExtension.create(),
      SuperscriptExtension.create(),
      TablesExtension.create(),
      TypographicExtension.create(),
      YouTubeLinkExtension.create()
  );

  private static final MutableDataSet TOPIC_OPTIONS = getCommonOptions()
      .set(Parser.EXTENSIONS, ImmutableList.<Extension>builder().addAll(POST_EXTENSIONS).add(
          AdmonitionExtension.create(),
          AnchorLinkExtension.create(),
          FootnoteExtension.create()
      ).build());

  private static final MutableDataSet POST_OPTIONS = getCommonOptions()
      .set(Parser.EXTENSIONS, POST_EXTENSIONS);

  @Getter
  private final Parser topicParser = Parser.builder(TOPIC_OPTIONS).build();
  @Getter
  private final HtmlRenderer topicRenderer = HtmlRenderer.builder(TOPIC_OPTIONS).build();
  @Getter
  private final Parser postParser = Parser.builder(POST_OPTIONS).build();
  @Getter
  private final HtmlRenderer postRenderer = HtmlRenderer.builder(POST_OPTIONS).build();

  public String renderTopic(String raw) {
    return topicRenderer.render(topicParser.parse(raw));
  }

  public String renderPost(String raw) {
    return postRenderer.render(postParser.parse(raw));
  }
}
