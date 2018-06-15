-- init groups
INSERT INTO groups(id, color, icon, name) VALUES (1, NULL, NULL, 'Admin');
INSERT INTO groups(id, color, icon, name) VALUES (2, NULL, NULL, 'Mod');
INSERT INTO groups(id, color, icon, name) VALUES (3, NULL, NULL, 'User');

select nextval('group_sequence');
select nextval('group_sequence');
select nextval('group_sequence');

-- init categories
INSERT INTO categories(id, description, name, position, slug, parent_id) VALUES
  (0, 'Home', 'home', 0, 'home', NULL);

-- init admin permissions
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.edit.own.title');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.edit.own.category');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.locked.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.delete.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'post.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'post.edit.own.content');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'post.delete.own');

INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'user.lock');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'ip.read');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'report.manage');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.edit.title');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.edit.category');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.locked');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.delete');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.pinned');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'topic.feature');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'post.edit.content');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'post.delete');

INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'admin');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'group.all');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'category.all');
INSERT INTO group_permissions(group_id, permissions) VALUES (1, 'setting.all');

-- init mod permissions
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.edit.own.title');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.edit.own.category');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.locked.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.delete.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'post.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'post.edit.own.content');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'post.delete.own');

INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'user.lock');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'ip.read');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'report.manage');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.edit.title');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.edit.category');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.locked');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.delete');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.pinned');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'topic.feature');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'post.edit.content');
INSERT INTO group_permissions(group_id, permissions) VALUES (2, 'post.delete');

-- init user permissions
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'topic.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'topic.edit.own.title');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'topic.edit.own.category');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'topic.locked.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'topic.delete.own');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'post.create');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'post.edit.own.content');
INSERT INTO group_permissions(group_id, permissions) VALUES (3, 'post.delete.own');

-- init secret

INSERT INTO secrets(key, value) VALUES ('userLinkFormat', '<a class="user-link" href="/user/%s">@%s </a>');
INSERT INTO secrets(key, value) VALUES ('url', 'http://localhost');
INSERT INTO secrets(key, value) VALUES ('mail.host', 'smtp.mailgun.org');
INSERT INTO secrets(key, value) VALUES ('mail.username', 'username');
INSERT INTO secrets(key, value) VALUES ('mail.password', 'password');
INSERT INTO secrets(key, value) VALUES ('mail.address', 'noreply@bicarb.org');
INSERT INTO secrets(key, value) VALUES ('mail.personal', 'Bicarb');

-- init setting

INSERT INTO settings(key, value) VALUES ('Content-Security-Policy', 'default-src ''self''; base-uri ''self''; script-src ''self'' ''nonce-%s'' https:; style-src ''self'' ''unsafe-inline'' https:; img-src ''self'' https: data:; font-src ''self'' https: data:; connect-src ''self'' ws://localhost; frame-src ''self'' https://www.youtube.com');
