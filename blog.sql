DROP SCHEMA IF EXISTS `blog`;

CREATE SCHEMA `blog`;

USE `blog`;

CREATE TABLE `user` (
	`id` bigint not null auto_increment,
    `username` varchar(50) not null,
    `email` varchar(100) not null,
    `password` varchar(100) not null,
    `role` varchar(15) not null default 'USER',
    `created_at` datetime default current_timestamp,
    `updated_at` datetime default current_timestamp on update current_timestamp,
    primary key (`id`),
    unique key `EMAIL_UNIQUE` (`email`)
) engine = InnoDB,
 auto_increment = 1,
 default charset = utf8mb4,
 collate= utf8mb4_unicode_ci;

-- key `FK_USER_idx` (`user_id`) can MySQL auto-generate if you don't write it

CREATE TABLE `post` (
	`id` bigint not null auto_increment,
    `user_id` bigint not null,
    `title` varchar(255) not null,
    `content` text not null,
    `created_at` datetime default current_timestamp,
    `updated_at` datetime default current_timestamp on update current_timestamp,
    key `FK_USER_idx` (`user_id`),
    constraint `FK_USER` foreign key (`user_id`) references `user` (`id`)
    on delete cascade on update no action,
    primary key (`id`)
) engine = InnoDB,
 auto_increment = 1,
 default charset = utf8mb4,
 collate= utf8mb4_unicode_ci;

CREATE TABLE `comment` (
	`id` bigint not null auto_increment,
    `user_id` bigint not null,
    `post_id` bigint not null,
    `content` text not null,
    `created_at` datetime default current_timestamp,
    `updated_at` datetime default current_timestamp on update current_timestamp,
    key `FK_USER05_idx` (`user_id`),
    key `FK_POST_idx` (`post_id`),
    constraint `FK_USER05` foreign key (`user_id`) references `user` (`id`)
    on delete cascade on update no action,
    constraint `FK_POST` foreign key (`post_id`) references `post` (`id`)
    on delete cascade on update no action,
    primary key (`id`)
) engine = InnoDB,
 auto_increment = 1,
 default charset = utf8mb4,
 collate= utf8mb4_unicode_ci;
