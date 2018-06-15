-- test data

-- user

select nextval('user_sequence');
select nextval('user_sequence');
select nextval('user_sequence');

INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (1, true, null, null, now(), 'user@user.com', false, null, null, null, null, null, 'user', '{bcrypt}$2a$10$1/BLjbARIvzWyV8wnwah../.AKsvLPYiczH8Bhu3ANSCjA5BQduK6', 0, 0, 'user', null, 3);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (2, true, null, null, now(), 'mod@mod.com', false, null, null, null, null, null, 'mod', '{bcrypt}$2a$10$CysKFxx2lVXICFLg1EviNOpwm3U/89/3ZT9P69WnlIRqvYFdHnW1C', 0, 0, 'mod', null, 2);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (3, true, null, null, now(), 'admin@admin.com', false, null, null, null, null, null, 'admin', '{bcrypt}$2a$10$yKxbfaHX7B4jdcu1XO1RlezYfKlX6OO3k6yMvzHZaF7KM5qolW38a', 0, 0, 'admin', null, 1);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (4, true, null, null, now(), 'bob@bob.com', false, null, null, null, null, null, 'bob', '{bcrypt}$2a$10$BCJcmVF9KB7m/1pgi3SsZee.SVebnqQojCqQ73y07Su//c4gCrg9u', 0, 0, 'bob', null, 3);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (5, true, null, null, now(), 'alice@alice.com', false, null, null, null, null, null, 'alice', '{bcrypt}$2a$10$42C038vYm3OnZdNenmUNT.PS0JzKGMGknoNxJDu0TQvcamcLWlJzm', 0, 0, 'alice', null, 3);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (6, false, null, null, now(), 'inactive@inactive.com', false, null, null, null, null, null, 'inactive', '{bcrypt}$2a$10$kAcdohZzJ13X3DRaE/wNU..kmTixA52QeuHbLomdHlFML2r7JB/Yq', 0, 0, 'inactive', null, 3);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (7, true, null, null, now(), 'locked@locked.com', false, null, null, null, now(), '2099-01-01', 'locked', '{bcrypt}$2a$10$IPqFqNP4C/dbQj.7KpfMlupFfqzXaa2Nuqx7NQqsl0NGFehRk3D.a', 0, 0, 'locked', null, 3);

select nextval('category_sequence');
select nextval('category_sequence');
select nextval('category_sequence');
select nextval('category_sequence');
select nextval('category_sequence');

-- category

INSERT INTO public.categories (id, description, name, position, slug, topic_count, parent_id) VALUES
  (1, null, '综合', 0, 'zong-he', 0, 0);
INSERT INTO public.categories (id, description, name, position, slug, topic_count, parent_id) VALUES
  (2, null, '啊', 1, 'a', 0, 0);
INSERT INTO public.categories (id, description, name, position, slug, topic_count, parent_id) VALUES
  (3, null, '呀', 2, 'ya', 0, 0);
INSERT INTO public.categories (id, description, name, position, slug, topic_count, parent_id) VALUES
  (4, null, '综合它儿', 0, 'zong-he-ta-er', 0, 1);
INSERT INTO public.categories (id, description, name, position, slug, topic_count, parent_id) VALUES
  (5, null, '综合它孙', 0, 'zong-he-ta-sun', 0, 4);

-- topic & post

select nextval('topic_sequence');
select nextval('post_sequence');
select nextval('post_sequence');

INSERT INTO public.topics (id, create_at, delete, feature, last_reply_at, locked, pinned, post_index, slug, title, author_id, last_reply_by_id) VALUES
  (1, now(), false, false, now(), false, false, 4, 'du-wo-yong-qian-bi-du', '【读我！】用前必读！', 1, 1);

INSERT INTO public.posts (id, cooked, create_at, delete, index, ip, last_edit_at, raw, author_id, topic_id) VALUES
  (1, '<h2>heading</h2>
<p><a class="user-link" href="/user/alice"><strong>@alice</strong> </a>Hello.</p>', now(), false, 0, '0:0:0:0:0:0:0:1', null, '## heading

@alice Hello.', 1, 1);
INSERT INTO public.posts (id, cooked, create_at, delete, index, ip, last_edit_at, raw, author_id, topic_id) VALUES
  (2, '<h2>heading</h2>', now(), false, 1, '0:0:0:0:0:0:0:1', null, '## heading', 1, 1);
INSERT INTO public.posts (id, cooked, create_at, delete, index, ip, last_edit_at, raw, author_id, topic_id) VALUES
  (3, '<h2>heading</h2>', now(), false, 2, '0:0:0:0:0:0:0:1', null, '## heading', 1, 1);
INSERT INTO public.posts (id, cooked, create_at, delete, index, ip, last_edit_at, raw, author_id, topic_id) VALUES
  (4, '<h2>heading</h2>', now(), false, 3, '0:0:0:0:0:0:0:1', null, '## heading', 1, 1);
INSERT INTO public.posts (id, cooked, create_at, delete, index, ip, last_edit_at, raw, author_id, topic_id) VALUES
  (5, '<h2>heading</h2>', now(), false, 4, '0:0:0:0:0:0:0:1', null, '## heading', 1, 1);

INSERT INTO public.topics_categories (topic_id, categories_id) VALUES (1, 5);
INSERT INTO public.topics_categories (topic_id, categories_id) VALUES (1, 4);
INSERT INTO public.topics_categories (topic_id, categories_id) VALUES (1, 1);
INSERT INTO public.topics_categories (topic_id, categories_id) VALUES (1, 0);

UPDATE public.categories SET topic_count = 1 WHERE id = 0 OR id = 1 OR id = 4 OR id = 5;
UPDATE public.users SET topic_count = 1, post_count = 4 WHERE id = 1;

-- setting

INSERT INTO public.settings (key, value) VALUES ('primaryColor', '#fff');
INSERT INTO public.settings (key, value) VALUES ('secondColor', '#fff');

-- report

select nextval('report_sequence');
select nextval('report_sequence');

INSERT INTO public.reports (id, create_at, handle_at, reason, by_id, post_id) VALUES
  (1, now(), null, 'reason', 1, 1);
INSERT INTO public.reports (id, create_at, handle_at, reason, by_id, post_id) VALUES
  (2, now(), null, 'reason', 2, 1);

-- notification

select nextval('notification_sequence');

INSERT INTO public.notifications (id, create_at, read_at, type, post_id, send_id, to_id, topic_id) VALUES
  (1, now(), null, 'MENTION', 1, 1, 5, 1);
