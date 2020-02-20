create table Users(
  `id` varchar(36) not null,
  `name` varchar (255) not null,
  `created_at` datetime(3) not null,
  primary key(`id`)
);

create table Rooms(
  `id` varchar(36) not null,
  `name` varchar (255) not null unique,
  `created_at` datetime(3) not null,
  primary key(`id`)
);

create table Messages(
  `id` varchar(36) not null,
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  `message` varchar (1024) not null,
  `created_at` datetime(3) not null,
  primary key(`id`),
  foreign key(`room_id`) references Rooms(`id`),
  foreign key(`user_id`) references Users(`id`),
  index idx_create_at(`created_at`)
);

create table Room_Users(
  `room_id` varchar(36) not null,
  `user_id` varchar(36) not null,
  primary key(`room_id`, `user_id`)
);

create table Auths(
  `email` varchar(255) not null,
  `hashed_password` varchar(255) not null,
  `user_id` varchar(36) not null unique,
  primary key (`email`),
  foreign key(`user_id`) references Users(`id`)
);

create table Tokens(
  `token` varchar(255) not null,
  `user_id` varchar(36) not null,
  `expires_at` datetime(3) not null,
  primary key (`token`),
  foreign key(`user_id`) references Users(`id`)
);
