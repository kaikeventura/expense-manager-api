create table users (
    id varchar(36) not null,
    created_at datetime(6),
    email varchar(100),
    modified_at datetime(6),
    name varchar(40),
    pass varchar(255),
    role enum ('USER','ADMIN')
) engine=InnoDB;

alter table users add primary key(id);

alter table users add constraint uk_user_email unique (email);
