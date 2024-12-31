package com.example.evatgbotmanager.service;

import com.example.evatgbotmanager.entity.PartnerPostBackMappingEntity;
import com.example.evatgbotmanager.entity.TelegramBotEntity;
import com.example.evatgbotmanager.repository.TelegramRepository;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerPostbackParserService {

    private final TelegramService telegramService;

    private final Map<String, PartnerPostBackMappingEntity> postBackMappingEntityCache = new HashMap<>();

    @Scheduled(fixedRate = 120_000)
    void invalidateCache() {
        List<TelegramBotEntity> all = telegramService.findAll();
        for (TelegramBotEntity entity : all) {
            postBackMappingEntityCache.put(entity.getDomainBot(), entity.getPartnerPostbackMapping());
        }
    }

    public PartnerPostBackMappingEntity getPartnerPostbackMapping(String domain) {
        return postBackMappingEntityCache.get(domain);
    }

}
