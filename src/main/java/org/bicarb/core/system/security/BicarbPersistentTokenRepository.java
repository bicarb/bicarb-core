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

package org.bicarb.core.system.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * BicarbPersistentTokenRepository.
 *
 * @author olOwOlo
 */
@Transactional
@Repository
public class BicarbPersistentTokenRepository implements PersistentTokenRepository {

  private final RememberMeTokenRepository rememberMeTokenRepository;

  @Autowired
  public BicarbPersistentTokenRepository(
      RememberMeTokenRepository rememberMeTokenRepository) {
    this.rememberMeTokenRepository = rememberMeTokenRepository;
  }

  @Override
  public void createNewToken(PersistentRememberMeToken token) {
    RememberMeToken rmToken = new RememberMeToken();
    rmToken.setUsername(token.getUsername());
    rmToken.setSeries(token.getSeries());
    rmToken.setToken(token.getTokenValue());
    rmToken.setLastUsed(token.getDate().toInstant());
    rememberMeTokenRepository.save(rmToken);
  }

  @Override
  public void updateToken(String series, String tokenValue, Date lastUsed) {
    // series not null
    rememberMeTokenRepository.updateTokenBySeries(series, tokenValue, lastUsed.toInstant());
  }

  @Override
  public PersistentRememberMeToken getTokenForSeries(String seriesId) {
    RememberMeToken rmToken = getTokenBySeriesId(seriesId);
    return rmToken == null
        ? null
        : new PersistentRememberMeToken(rmToken.getUsername(), rmToken.getSeries(),
            rmToken.getToken(), Date.from(rmToken.getLastUsed()));
  }

  @Override
  public void removeUserTokens(String username) {
    rememberMeTokenRepository.deleteByUsernameIgnoreCase(username);
  }

  private RememberMeToken getTokenBySeriesId(String seriesId) {
    return rememberMeTokenRepository.findById(seriesId).orElse(null);
  }
}
