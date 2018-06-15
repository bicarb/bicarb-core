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

package org.bicarb.core.forum.search;

import com.hankcs.lucene.HanLPTokenizerFactory;
import java.lang.annotation.ElementType;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.bicarb.core.forum.domain.Post;
import org.bicarb.core.forum.domain.Topic;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.TermVector;
import org.hibernate.search.bridge.builtin.IntegerBridge;
import org.hibernate.search.cfg.SearchMapping;

/**
 * Mapping indexed entities by programmatic API.
 *
 * @author olOwOlo
 */
public class BicarbSearchMappingFactory {

  /**
   * Manual update post index instead of annotated with
   * {@link org.hibernate.search.annotations.ContainedIn} on topic.posts, see
   * {@link org.bicarb.core.forum.search.TopicTitleUpdatePreCommit} and
   * {@link org.bicarb.core.forum.search.TopicDeleteUpdatePreCommit}.
   * @return SearchMapping
   */
  @Factory
  public SearchMapping getSearchMapping() {
    SearchMapping mapping = new SearchMapping();

    mapping
        // define cn analyzer
        .analyzerDef("cn", HanLPTokenizerFactory.class)
            .charFilter(HTMLStripCharFilterFactory.class)
            .filter(ASCIIFoldingFilterFactory.class)
            .filter(LowerCaseFilterFactory.class)
            .filter(SnowballPorterFilterFactory.class)
                .param("language", "English")
        // mapping Post
        .entity(Post.class)
        .indexed()
        .interceptor(NonDeletePostInterceptor.class)
        .property("cooked", ElementType.METHOD)
            .field()
                .termVector(TermVector.YES)
                .analyzer("cn")
        .property("topic", ElementType.METHOD)
            .indexEmbedded()
                .depth(1)
        .property("index", ElementType.METHOD)
            .field()
                .analyze(Analyze.NO)
                .norms(Norms.NO)
                .bridge(IntegerBridge.class)  // not Trie structure
        // mapping Topic
        .entity(Topic.class)
        .property("title", ElementType.METHOD)
            .field()
            .termVector(TermVector.YES)
            .analyzer("cn");

    return mapping;
  }
}
