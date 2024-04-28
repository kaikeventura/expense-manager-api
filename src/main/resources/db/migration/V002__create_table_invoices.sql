create table invoices (
    id varchar(36) not null,
    created_at datetime(6),
    modified_at datetime(6),
    reference_month varchar(7),
    state enum ('PREVIOUS','CURRENT','FUTURE'),
    total_value bigint not null,
    user_id varchar(36)
) engine=InnoDB;

alter table invoices add primary key(id);

alter table invoices add constraint fk_invoices_user_id
   foreign key (user_id) references users (id);
