drop table if exists users;
drop table if exists rooms;
drop table if exists messages;
drop table if exists room_users;

create table Users(
  `id` varchar(36) not null,
  `name` varchar (255) not null,
  `created_at` datetime not null,
  primary key(`id`)
);

create table Rooms(
  `id` varchar(36) not null,
  `name` varchar (255) not null unique,
  `created_at` datetime not null,
  primary key(`id`)
);

create table Messages(
  `id` varchar(36) not null,
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  `message` varchar (1024) not null,
  `created_at` datetime not null,
  primary key(`id`),
  foreign key(`room_id`) references Rooms(`id`),
  foreign key(`user_id`) references Users(`id`),
  index idx_create_at(`created_at`)
);

create table Room_Users(
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  primary key(`room_id`, `user_id`)
)
