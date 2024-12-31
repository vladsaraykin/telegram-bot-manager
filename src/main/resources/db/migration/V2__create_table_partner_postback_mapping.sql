create table IF NOT EXISTS partner_postback_mapping (
    id                    serial primary key,
    click_id               varchar(100),
    event_id               varchar(100),
    trader_id              varchar(100),
    status                varchar(100),
    registration          varchar(100),
    fist_replenishment     varchar(100),
    tg_user_id              varchar(100)
);

alter table telegram_bot
    add column partner_postback_mapping_id int references partner_postback_mapping;