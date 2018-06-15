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

package org.bicarb.core.forum.repository;

import java.util.List;
import org.bicarb.core.forum.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Category Repository.
 *
 * @author olOwOlo
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  @Query("select max(position) from Category where parent = ?1")
  Integer findMaxPositionWithSameParent(Category parent);

  @Query("select max(position) from Category where parent is null")
  Integer findMaxPositionInRoot();

  List<Category> findByParentOrderByPositionDesc(Category parent);

  boolean existsBySlug(String slug);

  @Modifying
  @Query(value = "insert into topics_categories select * from "
      + "(select topic_id from topics_categories where categories_id = :cid) a, "
      + "(select :addCid as categories_id) b", nativeQuery = true)
  void addRelationsForCategory(@Param("cid") Integer cid, @Param("addCid") Integer addCid);

  @Modifying
  @Query(value = "delete from topics_categories where categories_id = :removeCid and topic_id in"
      + " (select topic_id from topics_categories where categories_id = :cid)",
      nativeQuery = true)
  void removeRelationsForCategory(@Param("cid") Integer cid, @Param("removeCid") Integer removeCid);

  @Modifying
  @Query(value = "delete from topics_categories where categories_id = :cid", nativeQuery = true)
  void removeRelationsByCategoryId(@Param("cid") Integer cid);
}
