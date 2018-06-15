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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import javax.crypto.AEADBadTagException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.tuple.Pair;
import org.bicarb.core.forum.domain.Secret;
import org.bicarb.core.forum.repository.SecretRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Javascript Object Signing and Encryption Helper.
 *
 * @author olOwOlo
 */
@Component
public class JoseHelper {

  private static final Logger logger = LoggerFactory.getLogger(JoseHelper.class);

  public static final String JOSE_KEY = "jose-aes256-transient";

  public static final Duration JOSE_DURATION = Duration.ofHours(1);
  // subjects
  public static final String EMAIL_VERIFY = "email.verify";
  public static final String PASSWORD_RESET = "password.reset";

  private final LoadingCache<String, SecretKey> keyCache;

  /** Load or generate an AES key(transient). */
  @Autowired
  public JoseHelper(SecretRepository secretRepository) {
    keyCache = CacheBuilder.newBuilder()
        .expireAfterAccess(JOSE_DURATION)
        .build(new CacheLoader<>() {
          @Override
          public SecretKey load(@NonNull String key) throws NoSuchAlgorithmException {
            // create a new key and store/update it
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey k = keyGen.generateKey();
            Secret secret = new Secret(JOSE_KEY, BaseEncoding.base64().encode(k.getEncoded()));
            secretRepository.save(secret);
            logger.debug("Generated a new key for jose");
            return k;
          }
        });

    // load key if exist
    secretRepository.findById(JOSE_KEY).ifPresent(secret -> {
      byte[] bytes = BaseEncoding.base64().decode(secret.getValue());
      keyCache.put(JOSE_KEY, new SecretKeySpec(bytes, 0, bytes.length, "AES"));
      logger.debug("Put exist key to cache");
    });
  }

  private SecretKey getSecretKey() {
    try {
      return keyCache.get(JOSE_KEY);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sign and encrypt.
   *
   * @param claimsSet claimsSet
   * @return Base64URL-encoded String
   * @throws JOSEException JOSEException
   */
  public String signEncrypt(JWTClaimsSet claimsSet) throws JOSEException {
    SecretKey secretKey = getSecretKey();

    // Create HMAC signer
    JWSSigner signer = new MACSigner(secretKey.getEncoded());

    SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

    // Apply the HMAC
    signedJwt.sign(signer);

    // Create JWE object with signed JWT as payload
    JWEObject jweObject = new JWEObject(
        new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
            .contentType("JWT") // required to signal nested JWT
            .build(),
        new Payload(signedJwt));

    // Perform encryption
    jweObject.encrypt(new DirectEncrypter(secretKey.getEncoded()));

    // Serialise to JWE compact form
    return jweObject.serialize();
  }

  /**
   * Decrypt and verify, also check the expiration time & subject.
   *
   * @return {@literal Pair<Valid: Boolean, result?: JWTClaimsSet>}
   */
  public Pair<Boolean, JWTClaimsSet> decryptVerify(String jweString, String subject)
      throws JOSEException {

    try {
      SecretKey secretKey = getSecretKey();
      // Parse the JWE string
      JWEObject jweObject = JWEObject.parse(jweString);

      // Decrypt with shared key
      jweObject.decrypt(new DirectDecrypter(secretKey.getEncoded()));

      // Extract payload
      SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();

      // Payload not a signed JWT || Check the HMAC fail
      if (signedJwt == null || !signedJwt.verify(new MACVerifier(secretKey.getEncoded()))) {
        return Pair.of(false, null);
      }

      JWTClaimsSet claimsSet = signedJwt.getJWTClaimsSet();

      // Check Expiration Time || Check subject
      if (claimsSet.getExpirationTime().toInstant().isBefore(Instant.now())
          || !subject.equals(claimsSet.getSubject())) {
        return Pair.of(false, null);
      }

      return Pair.of(true, claimsSet);
    } catch (ParseException e) {
      // do nothing
    } catch (JOSEException e) {
      if (e.getCause() instanceof AEADBadTagException) {
        // do nothing
      } else {
        throw e;
      }
    }
    return Pair.of(false, null);
  }
}
