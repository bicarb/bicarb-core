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

import com.google.common.collect.ImmutableMap;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.bicarb.core.forum.domain.User;
import org.bicarb.core.system.config.BicarbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * MailService.
 *
 * @author olOwOlo
 */
@Service
public class MailService {

  private static final Logger logger = LoggerFactory.getLogger(MailService.class);

  private static final String VERIFY_TEMPLATE = "mail/verify.html";
  private static final String RESET_PASSWORD_TEMPLATE = "mail/resetPassword.html";

  private final InternetAddress from;
  private final String personal;

  private final JavaMailSender sender;
  private final TemplateEngine templateEngine;

  /**
   * Inject beans,
   * create a from(InternetAddress) instance,
   * reset mailSender(host, username, password).
   *
   * @throws UnsupportedEncodingException if the personal name
   *     can't be encoded in the given charset
   */
  @Autowired
  public MailService(
      JavaMailSender sender,
      TemplateEngine templateEngine,
      BicarbProperties bicarbProperties)
      throws UnsupportedEncodingException {
    this.sender = sender;
    this.templateEngine = templateEngine;

    // not only inject...
    BicarbProperties.Mail mail = bicarbProperties.getMail();
    this.personal = mail.getPersonal();
    this.from = new InternetAddress(mail.getAddress(),
        mail.getPersonal(), "utf-8");

    // reset sender
    JavaMailSenderImpl senderImpl = ((JavaMailSenderImpl) sender);
    senderImpl.setHost(mail.getHost());
    senderImpl.setUsername(mail.getUsername());
    senderImpl.setPassword(mail.getPassword());
  }

  /**
   * sendActiveMail.
   *
   * @param to user.email & user.username
   * @param verifyLink verifyLink
   */
  @Async
  public void sendActiveMail(User to, String verifyLink)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = sender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(from);

    helper.setTo(generateAddress(to));
    helper.setSubject("[" + personal + "] Please verify your email address.");

    Context ctx = new Context(null, ImmutableMap.of("user", to, "verifyLink", verifyLink));
    helper.setText(templateEngine.process(VERIFY_TEMPLATE, ctx), true);

    logger.debug("prepare email[type: '{}', to: '{}']", "verify", to.getEmail());

    sender.send(message);
  }

  /**
   * Send new password to user.
   * @param to user.email & user.username
   * @param jwe jwe String
   */
  @Async
  public void sendResetPasswordMail(User to, String jwe)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = sender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(from);

    helper.setTo(generateAddress(to));
    helper.setSubject("[" + personal + "] Reset Password Token.");

    Context ctx = new Context(null, ImmutableMap.of("user", to, "jwe", jwe));
    helper.setText(templateEngine.process(RESET_PASSWORD_TEMPLATE, ctx), true);

    logger.debug("prepare email[type: '{}', to: '{}']", "reset password", to.getEmail());

    sender.send(message);
  }

  private InternetAddress generateAddress(User user) throws UnsupportedEncodingException {
    return new InternetAddress(user.getEmail(), user.getUsername(), "utf-8");
  }
}
