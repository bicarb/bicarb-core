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

package org.bicarb.core.forum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.core.ErrorObjects;
import java.security.Principal;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.GroupRepository;
import org.bicarb.core.forum.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminService.
 *
 * @author olOwOlo
 */
@Service
public class AdminService {

  private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

  private final Elide elide;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  /**
   * Constructor.
   */
  @Autowired
  public AdminService(
      Elide elide,
      ObjectMapper objectMapper,
      UserRepository userRepository,
      GroupRepository groupRepository) {
    this.elide = elide;
    this.objectMapper = objectMapper;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  /**
   * Forward to elide [POST][/user], and then change group to admin.
   * @param requestBody requestBody
   * @param auth Principal
   * @return ElideResponse
   * @throws JsonProcessingException JsonProcessingException
   */
  @Transactional
  public ElideResponse registerAdmin(String requestBody, Principal auth)
      throws JsonProcessingException {
    if (userRepository.findAll(PageRequest.of(0, 1)).getTotalElements() != 0) {
      return new ElideResponse(HttpStatus.FORBIDDEN.value(),
          objectMapper.writeValueAsString(ErrorObjects.builder()
              .addError().withDetail("admin must be first user").build()));
    }

    logger.debug("Forward to Elide [POST][/user]");
    ElideResponse elideResponse = elide.post("/user", requestBody, auth);
    logger.debug("[POST][/user] result: {}, {}",
        elideResponse.getResponseCode(), elideResponse.getBody());

    if (elideResponse.getResponseCode() == HttpStatus.CREATED.value()) {
      User u = userRepository.findAll().get(0);
      u.setGroup(groupRepository.getOne(1));
      u.setActive(true);
      logger.debug("Created user: {}", u);
      return new ElideResponse(HttpStatus.CREATED.value(),
          elide.get("/user/" + u.getId(), null, auth).getBody());
    } else {
      return elideResponse;
    }
  }
}
