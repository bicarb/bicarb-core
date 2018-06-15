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

import static org.bicarb.core.system.bean.JoseHelper.EMAIL_VERIFY;
import static org.bicarb.core.system.bean.JoseHelper.JOSE_DURATION;
import static org.bicarb.core.system.bean.JoseHelper.PASSWORD_RESET;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.core.ErrorObjects;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.bean.JoseHelper;
import org.bicarb.core.system.config.BicarbProperties;
import org.bicarb.core.system.security.BicarbSessionRegistry;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

/**
 * UserService.
 *
 * @author olOwOlo
 */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  private final JoseHelper joseHelper;
  private final BicarbProperties bicarbProperties;
  private final Elide elide;
  private final BicarbSessionRegistry sessionRegistry;

  /**
   * Constructor.
   */
  @Autowired
  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      MailService mailService,
      JoseHelper joseHelper,
      BicarbProperties bicarbProperties,
      Elide elide,
      BicarbSessionRegistry sessionRegistry) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.mailService = mailService;
    this.joseHelper = joseHelper;
    this.bicarbProperties = bicarbProperties;
    this.elide = elide;
    this.sessionRegistry = sessionRegistry;
  }

  /**
   * update password.
   * @return {@literal Optional<Pair<422, ErrorObjects(wrong confirmPassword)>>}.
   */
  @Transactional
  public Optional<Pair<Integer, ErrorObjects>> patchPassword(String newPassword,
      String confirmPassword, Principal authentication) {

    User u = userRepository.getOne(AuthenticationUtils.fetchUserId(authentication));
    if (passwordEncoder.matches(confirmPassword, u.getPassword())) {
      u.setPassword(passwordEncoder.encode(newPassword));
      userRepository.save(u);
      return Optional.empty();
    } else {
      return Optional.of(Pair.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ErrorObjects.builder()
          .addError().withCode("4221").withDetail("wrong confirmPassword")
          .build()));
    }
  }

  /**
   * Send jwt(sign & encrypt) by email.
   * @return {@literal Optional<Pair<400, ErrorObjects(already active)>>}.
   */
  @Transactional(readOnly = true)
  public Optional<Pair<Integer, ErrorObjects>> sendActiveEmail(Principal authentication)
      throws UnsupportedEncodingException, MessagingException, JOSEException {

    User u = userRepository.getOne(AuthenticationUtils.fetchUserId(authentication));
    if (u.getActive()) {
      return Optional.of(Pair.of(HttpStatus.BAD_REQUEST.value(), ErrorObjects.builder()
          .addError().withDetail("your account is already active.").build()));
    }

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(EMAIL_VERIFY)
        .claim("uid", u.getId())
        .expirationTime(Date.from(Instant.now().plus(JOSE_DURATION)))
        .build();

    mailService.sendActiveMail(u, bicarbProperties.getUrl()
        + "/api/user/email/verify/" + joseHelper.signEncrypt(claimsSet));
    return Optional.empty();
  }

  /**
   * activeUserByJwe.
   *
   * @param jwe jweString
   * @return {@literal ModelAndView("activeUser",
   *     {"state": "success" | "alreadyActive" | "invalidJwe"})}
   */
  @Transactional
  public ModelAndView activeUserByJwe(String jwe) throws JOSEException {

    Pair<Boolean, JWTClaimsSet> result = joseHelper.decryptVerify(jwe, EMAIL_VERIFY);
    Map<String, Object> model = new HashMap<>();

    if (result.getLeft()) {
      User u = userRepository.getOne(((Long) result.getRight().getClaim("uid")).intValue());
      if (!u.getActive()) {
        u.setActive(true);
        sessionRegistry.expireSessionInformationByUsername(u.getUsername());
        model.put("state", "success");
      } else {
        model.put("state", "alreadyActive");
      }
    } else {
      model.put("state", "invalidJwe");
    }
    return new ModelAndView("activeUser", model,
        model.get("state").equals("success") ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
  }

  /**
   * sendResetPasswordJwe.
   * Even if the user does not exist, no error is returned.
   *
   * @param email email
   */
  @Transactional(readOnly = true)
  public void sendResetPasswordJwe(String email)
      throws JOSEException, UnsupportedEncodingException, MessagingException {
    Optional<User> u = userRepository.findByEmail(email);
    if (u.isPresent()) {
      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject(PASSWORD_RESET)
          .claim("uid", u.get().getId())
          .expirationTime(Date.from(Instant.now().plus(JOSE_DURATION)))
          .build();

      mailService.sendResetPasswordMail(u.get(), joseHelper.signEncrypt(claimsSet));
    }
  }

  /**
   * Reset password by jwe string.
   *
   * @param jwe jwe string
   * @param newPw new password
   * @return {@literal Optional<Pair<400, ErrorObjects(Invalid jwe)>>}
   */
  @Transactional
  public Optional<Pair<Integer, ErrorObjects>> resetPassword(String jwe, String newPw)
      throws JOSEException {

    Pair<Boolean, JWTClaimsSet> result = joseHelper.decryptVerify(jwe, PASSWORD_RESET);
    if (result.getLeft()) {
      User u = userRepository.getOne(((Long) result.getRight().getClaim("uid")).intValue());
      u.setPassword(passwordEncoder.encode(newPw));
      return Optional.empty();
    } else {
      return Optional.of(Pair.of(HttpStatus.BAD_REQUEST.value(), ErrorObjects.builder()
          .addError().withDetail("Invalid jwe").build()));
    }
  }

  /**
   * Load user by username first, and then pass id to Elide.
   *
   * @param idOrUsername idOrUsername
   * @param allRequestParams allRequestParams
   * @param auth Principal
   * @return ElideResponse
   */
  public ElideResponse getUserByIdOrUsername(
      String idOrUsername, Map<String, String> allRequestParams, Principal auth) {
    if (!StringUtils.isNumeric(idOrUsername)) {
      idOrUsername = userRepository.findByUsernameIgnoreCase(idOrUsername)
          .map(u -> u.getId().toString()).orElse(idOrUsername);
    }
    return elide.get("/user/" + idOrUsername, new MultivaluedHashMap<>(allRequestParams), auth);
  }
}
