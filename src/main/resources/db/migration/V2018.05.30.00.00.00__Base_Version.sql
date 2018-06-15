create table if not exists categories (
  id          integer            not null constraint categories_pkey primary key,
  description varchar(255),
  name        varchar(255)       not null,
  position    integer            not null,
  slug        varchar(255)       not null constraint uk_category_slug unique,
  topic_count integer default 0  not null,
  parent_id   integer,
  constraint uk_category_parent_position unique (parent_id, position)
);

create table if not exists groups (
  id    integer     not null constraint groups_pkey primary key,
  color varchar(255),
  icon  varchar(255),
  name  varchar(20) not null constraint uk_group_name unique
);

create table if not exists group_permissions (
  group_id    integer not null,
  permissions varchar(255),
  constraint uk_group_permission unique (group_id, permissions)  -- manual add
);

create table if not exists remember_me_token (
  series    varchar(255) not null constraint remember_me_token_pkey primary key,
  last_used timestamp,
  token     varchar(255),
  username  varchar(255)
);

-- manual: upper()
create index if not exists remember_me_token_username_index on remember_me_token (upper(username));

create table if not exists settings (
  key   varchar(255)   not null constraint settings_pkey primary key,
  value varchar(65535) not null
);

create table if not exists secrets (
  key   varchar(255)   not null constraint secrets_pkey primary key,
  value varchar(65535) not null
);

create table if not exists users (
  id              integer                 not null constraint users_pkey primary key,
  active          boolean default false   not null,
  avatar          varchar(255),
  bio             varchar(255),
  create_at       timestamp default now() not null,
  email           varchar(255)            not null constraint uk_user_email unique,
  email_public    boolean default false   not null,
  github          varchar(255),
  last_sign_in_at timestamp,
  last_sign_ip    varchar(255),
  locked_at       timestamp,
  locked_until    timestamp,
  nickname        varchar(30)             not null constraint uk_user_nickname unique,
  password        varchar(255)            not null,
  post_count      integer default 0       not null,
  topic_count     integer default 0       not null,
  username        varchar(30)             not null,
  website         varchar(255),
  group_id        integer default 3       not null
);

-- manual: upper()
create unique index if not exists uk_user_username on users (upper(username));

create table if not exists topics (
  id               integer                 not null constraint topics_pkey primary key,
  create_at        timestamp default now() not null,
  delete           boolean default false   not null,
  delete_by_id     integer,
  feature          boolean default false   not null,
  last_reply_at    timestamp default now() not null,
  locked           boolean default false   not null,
  locked_by_id     integer,
  pinned           boolean default false   not null,
  post_index       integer default 0       not null,
  slug             varchar(255)            not null,
  title            varchar(255)            not null,
  author_id        integer                 not null,
  last_reply_by_id integer
);

create index if not exists topics_author_index on topics (author_id);

create table if not exists posts (
  id           integer                 not null constraint posts_pkey primary key,
  cooked       text                    not null,
  create_at    timestamp default now() not null,
  delete       boolean default false   not null,
  delete_by_id integer,
  index        integer                 not null,
  ip           varchar(255)            not null,
  last_edit_at timestamp,
  raw          text                    not null,
  author_id    integer                 not null,
  topic_id     integer                 not null,
  constraint uk_topic_id_index unique (topic_id, index)
);

create index if not exists posts_author_index on posts (author_id);

create table if not exists notifications (
  id        integer                 not null constraint notifications_pkey primary key,
  create_at timestamp default now() not null,
  read_at   timestamp,
  type      varchar(255)            not null,
  post_id   integer                 not null,
  send_id   integer                 not null,
  to_id     integer                 not null,
  topic_id  integer                 not null
);

create index if not exists notifications_to_index on notifications (to_id);

create table if not exists reports (
  id        integer                 not null constraint reports_pkey primary key,
  create_at timestamp default now() not null,
  handle_at timestamp,
  reason    varchar(255)            not null,
  by_id     integer                 not null,
  post_id   integer                 not null
);

create index if not exists reports_post_index on reports (post_id);

create table if not exists topics_categories (
  topic_id      integer not null,
  categories_id integer not null,
  constraint topics_categories_pkey primary key (topic_id, categories_id)
);

create index if not exists topics_categories_categories on topics_categories (categories_id);

--- sequence

create sequence category_sequence;
create sequence group_sequence;
create sequence notification_sequence increment by 5;
create sequence post_sequence increment by 5;
create sequence report_sequence;
create sequence topic_sequence increment by 5;
create sequence user_sequence increment by 5;
