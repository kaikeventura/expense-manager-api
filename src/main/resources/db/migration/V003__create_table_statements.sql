create table statements (
    id varchar(36) not null,
    category enum ('FOOD','TRANSPORTATION','ENTERTAINMENT','HEALTH','UTILITIES','EDUCATION','SHOPPING','HOUSING','TRAVEL','PERSONAL_CARE','GIFTS','OTHER'),
    code varchar(36),
    created_at datetime(6),
    description varchar(255),
    modified_at datetime(6),
    type enum ('CREDIT_CARD','FIXED','IN_CASH'),
    value bigint not null,
    invoice_id varchar(36)
) engine=InnoDB;

alter table statements add primary key(id);

alter table statements add constraint fk_statements_invoice_id
   foreign key (invoice_id) references invoices (id);
