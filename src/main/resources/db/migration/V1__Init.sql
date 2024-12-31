create table IF NOT EXISTS analytic_info (
    id                    serial primary key,
    api_key               varchar
);

create table IF NOT EXISTS telegram_bot (
    id                    serial primary key,
    domain_bot            varchar(255) unique,
    description           varchar  ,
    introduction          varchar ,
    token                 varchar unique,
    url                   varchar,
    i18n                  varchar(15),
    analytic_id           int references analytic_info
);