INSERT INTO groups(id, color, icon, name) VALUES (233, '#fff', 'fa fa-player', 'Player');

INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (22, true, null, null, now(), '22@22.com', false, null, null, null, null, null, '22', 'pw', 0, 0, '22', null, 233);
INSERT INTO public.users (id, active, avatar, bio, create_at, email, email_public, github, last_sign_in_at, last_sign_ip, locked_at, locked_until, nickname, password, post_count, topic_count, username, website, group_id) VALUES
  (33, true, null, null, now(), '33@33.com', false, null, null, null, null, null, '33', 'pw', 0, 0, '33', null, 233);
