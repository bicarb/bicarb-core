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

package org.bicarb.core.forum.check;

import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.UserCheck;
import java.time.Instant;
import java.util.Optional;
import org.bicarb.core.system.security.BicarbUserDetails;
import org.bicarb.core.system.util.AuthenticationUtils;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionCheck.
 *
 * @author olOwOlo
 */
public class PermissionCheck {

  // admin
  // public static final String ADMIN = "admin"; // for spring security
  public static final String GROUP_ALL = "group.all";
  public static final String CATEGORY_ALL = "category.all";
  public static final String SETTING_ALL = "setting.all";

  // mod (delete all, include read and revert.)
  public static final String USER_LOCK = "user.lock";
  public static final String IP_READ = "ip.read";
  public static final String REPORT_MANAGE = "report.manage";
  public static final String TOPIC_EDIT_TITLE = "topic.edit.title";
  public static final String TOPIC_EDIT_CATEGORY = "topic.edit.category";
  public static final String TOPIC_LOCKED = "topic.locked";
  public static final String TOPIC_DELETE = "topic.delete";
  public static final String TOPIC_PINNED = "topic.pinned";
  public static final String TOPIC_FEATURE = "topic.feature";
  public static final String POST_EDIT_CONTENT = "post.edit.content";
  public static final String POST_DELETE = "post.delete";

  // user (restore/unlock require xxBy is owner)(require user is valid.)
  public static final String TOPIC_CREATE = "topic.create";
  public static final String TOPIC_EDIT_OWN_TITLE = "topic.edit.own.title";
  public static final String TOPIC_EDIT_OWN_CATEGORY = "topic.edit.own.category";
  public static final String TOPIC_LOCKED_OWN = "topic.locked.own";
  public static final String TOPIC_DELETE_OWN = "topic.delete.own";
  public static final String POST_CREATE = "post.create";
  public static final String POST_EDIT_OWN_CONTENT = "post.edit.own.content";
  public static final String POST_DELETE_OWN = "post.delete.own";

  private static final Logger logger = LoggerFactory.getLogger(PermissionCheck.class);

  /**
   * BasePermissionCheck, Extend this and implement <code>getPermission</code>.
   */
  public abstract static class BasePermissionCheck extends UserCheck {

    @Override
    public boolean ok(User user) {
      Optional<BicarbUserDetails> o = AuthenticationUtils.getUserDetails(user);

      logger.debug("Check permission '{}' from UserDetails '{}' return {}",
          getPermission(),
          o.map(details -> "[" + details.getId() + "]").orElse("[null]"),
          o.isPresent() && o.get().hasPermission(getPermission()));

      return o.isPresent() && o.get().hasPermission(getPermission());
    }

    protected abstract String getPermission();
  }

  /**
   * Not admin or mod, is user.
   * These permissions also require user is valid.(active and non-locked)
   */
  public abstract static class UserPermissionCheck extends BasePermissionCheck {

    @Override
    public boolean ok(User user) {
      Optional<BicarbUserDetails> o = AuthenticationUtils.getUserDetails(user);

      logger.debug("Check valid user(active and non-locked) from UserDetails '{}' return {}",
          o.map(details -> "[" + details.getId() + "]").orElse("[null]"),
          o.isPresent() && o.get().getActive() && (o.get().getLockedUntil() == null
              || o.get().getLockedUntil().isBefore(Instant.now())));

      return super.ok(user)
          && o.isPresent()
          && o.get().getActive()
          && (o.get().getLockedUntil() == null || o.get().getLockedUntil().isBefore(Instant.now()));
    }
  }

  @ElideCheck(GROUP_ALL)
  public static class GroupAllPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return GROUP_ALL;
    }
  }

  @ElideCheck(CATEGORY_ALL)
  public static class CategoryAllPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return CATEGORY_ALL;
    }
  }

  @ElideCheck(SETTING_ALL)
  public static class SettingAllPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return SETTING_ALL;
    }
  }

  // ------------------------------------------------------

  @ElideCheck(USER_LOCK)
  public static class UserLockPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return USER_LOCK;
    }
  }

  @ElideCheck(IP_READ)
  public static class IpReadPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return IP_READ;
    }
  }

  @ElideCheck(REPORT_MANAGE)
  public static class ReportReadPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return REPORT_MANAGE;
    }
  }

  @ElideCheck(TOPIC_EDIT_TITLE)
  public static class TopicEditTitlePermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_EDIT_TITLE;
    }
  }

  @ElideCheck(TOPIC_EDIT_CATEGORY)
  public static class TopicEditCategoryPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_EDIT_CATEGORY;
    }
  }

  @ElideCheck(TOPIC_LOCKED)
  public static class TopicLockedPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_LOCKED;
    }
  }

  @ElideCheck(TOPIC_DELETE)
  public static class TopicDeletePermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_DELETE;
    }
  }

  @ElideCheck(TOPIC_PINNED)
  public static class TopicPinnedPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_PINNED;
    }
  }

  @ElideCheck(TOPIC_FEATURE)
  public static class TopicFeaturePermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_FEATURE;
    }
  }

  @ElideCheck(POST_EDIT_CONTENT)
  public static class PostEditContentPermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return POST_EDIT_CONTENT;
    }
  }

  @ElideCheck(POST_DELETE)
  public static class PostDeletePermissionCheck extends BasePermissionCheck {

    @Override
    protected String getPermission() {
      return POST_DELETE;
    }
  }

  // ------------------------------------------------------

  @ElideCheck(TOPIC_CREATE)
  public static class TopicCreatePermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_CREATE;
    }
  }

  @ElideCheck(TOPIC_EDIT_OWN_TITLE)
  public static class TopicEditOwnTitlePermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_EDIT_OWN_TITLE;
    }
  }

  @ElideCheck(TOPIC_EDIT_OWN_CATEGORY)
  public static class TopicEditOwnCategoryPermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_EDIT_OWN_CATEGORY;
    }
  }

  @ElideCheck(TOPIC_LOCKED_OWN)
  public static class TopicLockedOwnPermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_LOCKED_OWN;
    }
  }

  @ElideCheck(TOPIC_DELETE_OWN)
  public static class TopicDeleteOwnPermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return TOPIC_DELETE_OWN;
    }
  }

  @ElideCheck(POST_CREATE)
  public static class PostCreatePermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return POST_CREATE;
    }
  }

  @ElideCheck(POST_EDIT_OWN_CONTENT)
  public static class PostEditOwnContentPermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return POST_EDIT_OWN_CONTENT;
    }
  }

  @ElideCheck(POST_DELETE_OWN)
  public static class PostDeleteOwnPermissionCheck extends UserPermissionCheck {

    @Override
    protected String getPermission() {
      return POST_DELETE_OWN;
    }
  }
}
