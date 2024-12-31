package com.example.evatgbotmanager.service;

import com.example.evatgbotmanager.dto.AnalyticMetaData;
import com.example.evatgbotmanager.entity.TelegramBotEntity;
import com.example.evatgbotmanager.repository.TelegramRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class AmplitudeAnalyticService implements AnalyticService {

    private final WebClient webClient;
    private final TelegramRepository telegramRepository;
    private final Map<String, String> apiKeysMap = new HashMap<>();

    public AmplitudeAnalyticService(WebClient webClient, TelegramRepository telegramRepository) {
        this.webClient = webClient;
        this.telegramRepository = telegramRepository;
    }

    @Override
    public Mono<String> sendEvent(AnalyticMetaData analyticMetaData) {
        String apiKey = apiKeysMap.get(analyticMetaData.sourceDomain());
        if (StringUtils.isBlank(apiKey)) {
            log.warn("API key not registered {}", analyticMetaData.sourceDomain());
            return Mono.empty();
        }
        String userId = analyticMetaData.traderId();
        if (StringUtils.isBlank(userId)) {
            userId = analyticMetaData.telegramUserId();
        }
        var data = new HashMap<String, Object>();
        var events = new HashMap<String, Object>();
        data.put("api_key", apiKey);
        events.put("user_id", userId);
        events.put("device_id", analyticMetaData.deviceId());
        events.put("event_type", analyticMetaData.eventType());
        var eventProperties = new HashMap<>();
        eventProperties.put("tg_user_id", analyticMetaData.telegramUserId());
        eventProperties.put("click_id", analyticMetaData.clickId());
        eventProperties.put("payload", analyticMetaData.payload());
        if (!analyticMetaData.telegramParams().isEmpty()) {
            eventProperties.putAll(analyticMetaData.telegramParams());
        }
        events.put("event_properties", eventProperties);
        data.put("events", List.of(events));

        return webClient.post()
                .uri("https://api2.amplitude.com/2/httpapi")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .bodyValue(data)
                .exchangeToMono(s -> {
                    HttpStatusCode httpStatusCode = s.statusCode();
                    if (httpStatusCode.isError()) {
                        log.error("Failed send event analytic for domain {}", analyticMetaData.sourceDomain());
                    }
                    return s.bodyToMono(new ParameterizedTypeReference<byte[]>() {
                    }).map(e -> new String(e, StandardCharsets.UTF_8));
                })
                .doOnSuccess(s -> log.info("{} Response from amplitude {}", analyticMetaData.sourceDomain(), s));
    }

    @Override
    @Scheduled(fixedRate = 120_000)
    public void invalidateCache() {
        telegramRepository.findAll()
                .stream()
                .filter(entity -> entity.getAnalyticInfo() != null)
                .filter(entity -> entity.getAnalyticInfo().getApiKey() != null)
                .forEach(entity ->
                        apiKeysMap.put(entity.getDomainBot().toLowerCase(), entity.getAnalyticInfo().getApiKey())
                );
    }
}
