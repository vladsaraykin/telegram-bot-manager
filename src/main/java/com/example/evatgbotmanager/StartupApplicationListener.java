package com.example.evatgbotmanager;

import com.example.evatgbotmanager.entity.PartnerPostBackMappingEntity;
import com.example.evatgbotmanager.entity.TelegramBotEntity;
import com.example.evatgbotmanager.repository.PartnerPostbackMappingRepository;
import com.example.evatgbotmanager.repository.TelegramRepository;
import com.example.evatgbotmanager.service.TelegramService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private final TelegramService telegramService;
    private final TelegramRepository telegramRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<TelegramBotEntity> all = telegramService.findAll();
        for (TelegramBotEntity entity : all) {
            if (entity.getPartnerPostbackMapping() == null) {
                entity.setPartnerPostbackMapping(telegramService.defaultPostbackMapping());
                telegramRepository.save(entity);
            }
        }

        for (TelegramBotEntity entity : all) {
            telegramService.runBot(entity);
        }
    }
}
