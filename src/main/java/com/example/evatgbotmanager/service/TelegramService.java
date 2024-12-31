package com.example.evatgbotmanager.service;

import com.example.evatgbotmanager.dto.AnalyticMetaData;
import com.example.evatgbotmanager.dto.RegisterTelegramDto;
import com.example.evatgbotmanager.entity.AnalyticInfoEntity;
import com.example.evatgbotmanager.entity.PartnerPostBackMappingEntity;
import com.example.evatgbotmanager.entity.TGMessageEntity;
import com.example.evatgbotmanager.entity.TelegramBotEntity;
import com.example.evatgbotmanager.repository.TelegramRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.WebAppInfo;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramRepository telegramRepository;
    private final AnalyticServiceManager analyticServiceManager;
    private final MessageSource messageSource;

    private final Map<String, TelegramBot> registeredBots = new HashMap<>();


    @Transactional
    public TelegramBotEntity saveBot(RegisterTelegramDto registerTelegramDto) {
        List<TGMessageEntity> messages = new ArrayList<>();
       var tgBot = TelegramBotEntity.builder()
                .messages(messages)
                .domainBot(registerTelegramDto.getDomainBot())
                .analyticInfo(Optional.ofNullable(registerTelegramDto.getAnalyticInfo())
                        .map(s -> AnalyticInfoEntity.builder()
                                .apiKey(s.apiKey())
                                .build())
                        .orElse(null)
                )
                .url(registerTelegramDto.getUrl())
                .token(registerTelegramDto.getToken())
                .partnerPostbackMapping(defaultPostbackMapping())
                .build();
        Stream<TGMessageEntity> descriptionMsg = registerTelegramDto.getDescriptions().stream()
                .map(s -> TGMessageEntity.builder().telegramBot(tgBot).code("description_msg").msg(s.msg()).i18n(s.i18n()).build());
        Stream<TGMessageEntity> introMsg = registerTelegramDto.getDescriptions().stream()
                .map(s -> TGMessageEntity.builder().telegramBot(tgBot).code("intro_msg").msg(s.msg()).i18n(s.i18n()).build());
        messages.addAll(Stream.concat(descriptionMsg, introMsg).toList());
        return telegramRepository.save(tgBot);
    }

    public PartnerPostBackMappingEntity defaultPostbackMapping() {
        return PartnerPostBackMappingEntity.builder()
                .eventId("eid")
                .clickId("cid")
                .traderId("trader_id")
                .fistReplenishment("ftd")
                .registration("reg")
                .status("status")
                .tgUserId("tg_user_id")
                .build();
    }

    public List<TelegramBotEntity> findAll() {
        return telegramRepository.findAll();
    }

    public void runBot(TelegramBotEntity entity) {
        TelegramBot bot = new TelegramBot(entity.getToken());
        bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    Locale locale;
                    if (isStartMessage(update.message())) {
                        String languageCode = update.message().from().languageCode();
                        locale = Optional.ofNullable(Locale.of(languageCode)).orElse(Locale.ENGLISH);
                        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup()
                                .addRow(new InlineKeyboardButton(messageSource.getMessage("web_app_btn", null, locale)).webApp(new WebAppInfo(entity.getUrl())))
                                .addRow(new InlineKeyboardButton(messageSource.getMessage("intro_btn", null, locale)).callbackData("intro"));
                        bot.execute(new SendMessage(
                                    update.message().chat().id(),
                                    messageSource.getMessage(entity.getId() + "_intro_msg", null, locale))
                                .replyMarkup(keyboard)
                                .parseMode(ParseMode.Markdown)
                        );
                        sendStartEventToAnalytic(update, entity);
                    } else if (update.callbackQuery() != null) {
                        String languageCode = update.callbackQuery().from().languageCode();
                        locale = Optional.ofNullable(Locale.of(languageCode)).orElse(Locale.ENGLISH);

                        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.callbackQuery().id());
                        AnswerCallbackQuery allGood = answerCallbackQuery.text("ok");
                        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup()
                                .addRow(new InlineKeyboardButton(messageSource.getMessage("intro_btn", null, locale)).webApp(new WebAppInfo(entity.getUrl())));
                        bot.execute(allGood);
                        bot.execute(new SendMessage(
                                update.callbackQuery().maybeInaccessibleMessage().chat().id(),
                                messageSource.getMessage(entity.getId() + "_description_msg", null, locale))
                                .parseMode(ParseMode.Markdown)
                                .replyMarkup(keyboard)
                        );
                    }
                }
            } catch (Exception e) {
                log.error("{} Something wrong!!!", entity.getDomainBot(), e);
            }
            return CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                // got bad response from telegram
                log.error("{} Error code {} description {}", entity.getDomainBot(), e.response().errorCode(), e.response().description());
            } else {
                log.error("{} Unhandled error", entity.getDomainBot(), e);
            }
        });
        registeredBots.put(entity.getDomainBot(), bot);
        log.info("Telegram bot {} is started", entity.getDomainBot());
    }

    private void sendStartEventToAnalytic(Update update, TelegramBotEntity entity) {
        String text = update.message().text();
        String analyticTgKey = "";
        String analyticTgValue = "";
        if (text.startsWith("/start ")) {
            var s = text.substring(7, text.length() - 1);
            var arr = s.split("=");
            if (arr.length == 2) {
                analyticTgKey = arr[0];
                analyticTgValue = arr[1];
            } else {
                log.warn("Incorrect deep link for /start command. {}", text);
            }
        }
        log.debug("domainName {} device_d{} user_id {}, tg_user_login {} {}={}",
                entity.getDomainBot(),
                InetAddress.getLoopbackAddress().getHostAddress(),
                update.message().chat().id(),
                update.message().chat().username(),
                analyticTgKey, analyticTgValue);
        var analyticMetaData = new AnalyticMetaData(
                entity.getDomainBot(),
                "start_tg_bot",
                "",
                "",
                String.valueOf(update.message().chat().id()),
                "",
                InetAddress.getLoopbackAddress().getHostAddress(),
                Map.of(analyticTgKey, analyticTgValue),
                Map.of()

        );
        analyticServiceManager.sendEvent(analyticMetaData);
    }

    private boolean isStartMessage(Message message) {
        return message != null && message.text() != null && message.text().startsWith("/start");
    }

    public String stopBot(String botName) {
        TelegramBot telegramBot = registeredBots.get(botName);
        telegramBot.shutdown();
        registeredBots.remove(botName);
        return "success";
    }

    public String deleteBot(String domainBot) {
        var bot = telegramRepository.findByDomainBot(domainBot);
        telegramRepository.delete(bot);
        registeredBots.remove(domainBot);
        return "success";
    }
}
