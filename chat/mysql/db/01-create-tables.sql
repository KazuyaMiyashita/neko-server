drop table if exists users;
drop table if exists rooms;
drop table if exists messages;
drop table if exists room_users;

create table users(
  `id` varchar(36) not null,
  `name` varchar (255) not null,
  `create_at` datetime not null,
  primary key(`id`)
);

create table rooms(
  `id` varchar(36) not null,
  `name` varchar (255) not null,
  `create_at` datetime not null,
  primary key(`id`)
);

create table messages(
  `id` varchar(36) not null,
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  `message` varchar (1024) not null,
  `create_at` datetime not null,
  primary key(`id`),
  foreign key(`room_id`) references rooms(`id`),
  foreign key(`user_id`) references users(`id`),
  index idx_create_at(`create_at`)
);

create table room_users(
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  primary key(`room_id`, `user_id`)
)
