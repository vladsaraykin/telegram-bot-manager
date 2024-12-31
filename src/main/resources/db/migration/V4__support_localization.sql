create table IF NOT EXISTS tg_message
(
    id              serial primary key,
    code            varchar(100),
    msg             varchar,
    i18n            varchar(100),
    telegram_bot_id int references telegram_bot
);

insert into tg_message (code, msg, i18n, telegram_bot_id)
    (select 'intro_msg' as code, introduction, i18n, id from telegram_bot);

insert into tg_message (code, msg, i18n, telegram_bot_id)
    (select 'description_msg' as code, description, i18n, id from telegram_bot);

alter table telegram_bot drop column introduction;
alter table telegram_bot drop column description;
alter table telegram_bot drop column i18n;