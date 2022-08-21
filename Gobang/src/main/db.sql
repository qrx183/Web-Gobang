create database if not exists gobang;

use gobang;

drop table if exists user;

create table user (
    userId int primary key auto_increment,
    username varchar(60) unique,
    password varchar(60),
    score int,
    totalCount int,
    winCount int
);

insert into user value (null,'zhangsan','123',1000,0,0);
insert into user value (null,'qiusi','123',1000,0,0);
insert into user value (null,'taowu','123',1000,0,0);