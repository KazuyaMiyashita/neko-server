create table users(
  `id` varchar(36) not null,
  `screen_name` varchar (255) not null,
  `created_at` datetime(3) not null,
  primary key(`id`)
);

create table messages(
  `id` varchar(36) not null,
  `user_id` varchar(36) not null,
  `body` varchar (1024) not null,
  `created_at` datetime(3) not null,
  primary key(`id`),
  foreign key(`user_id`) references users(`id`),
  index idx_create_at(`created_at`)
);

create table auths(
  `login_name` varchar(255) not null,
  `hashed_password` varchar(255) not null,
  `user_id` varchar(36) not null unique,
  primary key (`login_name`),
  foreign key(`user_id`) references users(`id`)
);

create table tokens(
  `token` varchar(255) not null,
  `user_id` varchar(36) not null,
  `expires_at` datetime(3) not null,
  primary key (`token`),
  foreign key(`user_id`) references users(`id`)
);
