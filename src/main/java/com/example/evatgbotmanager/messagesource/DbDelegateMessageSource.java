package com.example.evatgbotmanager.messagesource;

import com.example.evatgbotmanager.entity.TGMessageEntity;
import com.example.evatgbotmanager.repository.TGMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DbDelegateMessageSource extends AbstractMessageSource {

    private final MessageSource parentMessageSource;
    private final TGMessageRepository tgMessageRepository;
    private final Map<String, Map<Locale, String>> cacheProperties = new ConcurrentHashMap<>();

    @PostConstruct
    void initCacheProperties() {
        for (TGMessageEntity entity : tgMessageRepository.findAll()) {
            Map<Locale, String> localeStringMap = cacheProperties.computeIfAbsent(entity.getCodKey(), k -> new HashMap<>());
            localeStringMap.put(Locale.of(entity.getI18n()), entity.getMsg());
        }
    }
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = parentMessageSource.getMessage(code, null, locale);
        if (message.equals(code)) {
            message = Optional
                    .ofNullable(cacheProperties.get(code))
                    .map(s -> {
                        String msg = s.get(locale);
                        if (msg == null) {
                            msg = s.get(Locale.ENGLISH);
                        }
                        return msg;
                    })
                    .orElse(message);
        }
        return new MessageFormat(message);
    }
}
