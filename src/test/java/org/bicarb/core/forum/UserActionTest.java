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

package org.bicarb.core.forum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bicarb.core.system.bean.JoseHelper.EMAIL_VERIFY;
import static org.bicarb.core.system.bean.JoseHelper.PASSWORD_RESET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.MimeMessage;
import org.bicarb.core.BaseSetup;
import org.bicarb.core.TimeAssert;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.forum.repository.UserRepository;
import org.bicarb.core.system.bean.JoseHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Non json api tests.
 *
 * @author olOwOlo
 */
public class UserActionTest extends BaseSetup {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private JoseHelper joseHelper;
  @SpyBean
  private JavaMailSender sender;

  @WithAnonymousUser
  @Test
  void testAuth() throws Exception {
    mockMvc.perform(formLogin().password("user"))
        .andExpect(status().isOk());

    User u = userRepository.getOne(1);
    TimeAssert.assertNow(u.getLastSignInAt());
    assertThat(u.getLastSignIp()).isIn("127.0.0.1", "0:0:0:0:0:0:0:1");

    mockMvc.perform(logout()).andExpect(status().isFound());
    mockMvc.perform(formLogin()).andExpect(status().isUnauthorized());
  }

  @Test
  void testGetUserByUserName() throws Exception {
    mockRequest.get(mockMvc, "/api/user/alice?include=group")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("5"))
        .andExpect(jsonPath("$.included[0].type").value("group"))
        .andExpect(jsonPath("$.included[0].id").value("3"));
  }

  @Test
  void testUpdatePasswordWrongConfirmPassword() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/user/password", "newPassword=new-pw&confirmPassword=wrong")
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.length()").value(1))
        .andExpect(jsonPath("$.errors[0].code").value("4221"))
        .andExpect(jsonPath("$.errors[0].detail").value("wrong confirmPassword"));
  }

  @Test
  void testUpdatePassword() throws Exception {
    mockRequest.patchForm(mockMvc, "/api/user/password", "newPassword=new-pw&confirmPassword=user")
        .andExpect(status().isNoContent());

    User u = userRepository.getOne(1);
    assertThat(passwordEncoder.matches("new-pw", u.getPassword())).isTrue();
  }

  // verify email / active user

  @WithUserDetails("inactive")
  @Test
  void testSendActiveEmail() throws Exception {
    doNothing().when(sender).send(any((MimeMessage.class)));

    mockRequest.postNoBody(mockMvc, "/api/user/email/verify/send")
        .andExpect(status().isOk());

    verifySendTimes(1);
  }

  @Test
  void testSendActiveEmailWithAlreadyActive() throws Exception {
    mockRequest.postNoBody(mockMvc, "/api/user/email/verify/send")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].detail").value("your account is already active."));

    verifySendTimes(0);
  }

  @Test
  void testActiveByJwe() throws Exception {
    JWTClaimsSet claimsSet = generateJwe(EMAIL_VERIFY, 6);
    mockMvc.perform(get("/api/user/email/verify/" + joseHelper.signEncrypt(claimsSet)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("state", "success"));

    assertThat(userRepository.getOne(6).getActive()).isTrue();
  }

  @Test
  void testActiveByWrongJwe() throws Exception {
    mockMvc.perform(get("/api/user/email/verify/wrong"))
        .andExpect(status().isBadRequest())
        .andExpect(model().attribute("state", "invalidJwe"));
  }

  @Test
  void testActiveByJweAlreadyActive() throws Exception {
    JWTClaimsSet claimsSet = generateJwe(EMAIL_VERIFY, 1);
    mockMvc.perform(get("/api/user/email/verify/" + joseHelper.signEncrypt(claimsSet)))
        .andExpect(status().isBadRequest())
        .andExpect(model().attribute("state", "alreadyActive"));
  }

  // end verify email / active user

  // reset password

  @WithAnonymousUser
  @Test
  void testSendResetPasswordJwe() throws Exception {
    doNothing().when(sender).send(any((MimeMessage.class)));

    mockRequest.postForm(mockMvc, "/api/user/password/reset/send", "email=user@user.com")
        .andExpect(status().isOk());

    verifySendTimes(1);
  }

  @WithAnonymousUser
  @Test
  void testSendResetPasswordJweWrongEmail() throws Exception {
    mockRequest.postForm(mockMvc, "/api/user/password/reset/send", "email=wrong@wrong.com")
        .andExpect(status().isOk());

    verifySendTimes(0);
  }

  @WithAnonymousUser
  @Test
  void testSendResetPasswordJweInvalidEmail() throws Exception {
    mockRequest.postForm(mockMvc, "/api/user/password/reset/send", "email=invalid")
        .andExpect(status().isBadRequest());

    verifySendTimes(0);
  }

  @WithAnonymousUser
  @Test
  void testResetPassword() throws Exception {
    JWTClaimsSet claimsSet = generateJwe(PASSWORD_RESET, 1);
    mockRequest.postForm(mockMvc, "/api/user/password/reset",
        "jwe=" + joseHelper.signEncrypt(claimsSet) + "&newPw=newPw")
        .andExpect(status().isOk());

    assertThat(passwordEncoder.matches("newPw", userRepository.getOne(1).getPassword())).isTrue();
  }

  @WithAnonymousUser
  @Test
  void testResetPasswordWrongJwe() throws Exception {
    mockRequest.postForm(mockMvc, "/api/user/password/reset",
        "jwe=wrong&newPw=newPw")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].detail").value("Invalid jwe"));
  }

  // end reset password

  @WithAnonymousUser
  @Test
  void testJwtExpiration() throws Exception {
    JWTClaimsSet claimsSet = generateJwe(PASSWORD_RESET, 1, Duration.ofNanos(0));
    mockRequest.postForm(mockMvc, "/api/user/password/reset",
        "jwe=" + joseHelper.signEncrypt(claimsSet) + "&newPw=newPw")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].detail").value("Invalid jwe"));
  }

  @WithAnonymousUser
  @Test
  void testJwtWrongSubject() throws Exception {
    JWTClaimsSet claimsSet = generateJwe(EMAIL_VERIFY, 1);
    mockRequest.postForm(mockMvc, "/api/user/password/reset",
        "jwe=" + joseHelper.signEncrypt(claimsSet) + "&newPw=newPw")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].detail").value("Invalid jwe"));
  }

  // ---

  private JWTClaimsSet generateJwe(String subject, Integer uid) {
    return generateJwe(subject, uid, Duration.ofHours(1));
  }

  private JWTClaimsSet generateJwe(String subject, Integer uid, Duration duration) {
    return new JWTClaimsSet.Builder()
        .subject(subject)
        .claim("uid", uid)
        .expirationTime(Date.from(Instant.now().plus(duration)))
        .build();
  }

  private void verifySendTimes(int times) throws InterruptedException {
    // wait for async
    TimeUnit.MILLISECONDS.sleep(500);
    verify(sender, Mockito.times(times)).send(any(MimeMessage.class));
  }
}
