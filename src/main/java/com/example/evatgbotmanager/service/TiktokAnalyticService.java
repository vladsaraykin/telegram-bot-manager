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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TiktokAnalyticService implements AnalyticService {

    private final WebClient webClient;
    private final TelegramRepository telegramRepository;

    private final Map<String, TiktokData> tiktokDataMap = new HashMap<>();

    public TiktokAnalyticService(WebClient webClient, TelegramRepository telegramRepository) {
        this.webClient = webClient;
        this.telegramRepository = telegramRepository;
    }

    @Override
    public Mono<String> sendEvent(AnalyticMetaData analyticMetaData) {
        TiktokData tiktokData = tiktokDataMap.get(analyticMetaData.sourceDomain());
        if (StringUtils.isBlank(analyticMetaData.sourceDomain()) || tiktokData == null) {
            log.warn("API key not registered {}", analyticMetaData.sourceDomain());
            return Mono.empty();
        }

        var data = new HashMap<String, Object>();
        data.put("event_source", "web");
        data.put("event_source_id", tiktokData.eventSourceId);
        var eventParams = new HashMap<String, Object>();
        eventParams.put("event", analyticMetaData.eventType());
        eventParams.put("event_time", Instant.now().getEpochSecond());
        eventParams.put("user", Map.of("external_id", analyticMetaData.telegramUserId()));
        data.put("data", List.of(eventParams));
        return webClient.post()
                .uri("https://business-api.tiktok.com/open_api/v1.3/event/track/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .header("Access-Token", tiktokData.accessToken)
                .bodyValue(data)
                .exchangeToMono(s -> {
                    HttpStatusCode httpStatusCode = s.statusCode();
                    if (httpStatusCode.isError()) {
                        log.error("Failed send event analytic for domain {}", analyticMetaData.sourceDomain());
                    }
                    return s.bodyToMono(new ParameterizedTypeReference<byte[]>() {
                    }).map(e -> new String(e, StandardCharsets.UTF_8));
                })
                .doOnSuccess(s -> log.info("{} Response from tiktok {}", analyticMetaData.sourceDomain(), s));
    }

    @Override
    @Scheduled(fixedRate = 120_000)
    public void invalidateCache() {
        telegramRepository.findAll()
                .stream()
                .filter(entity -> entity.getAnalyticInfo() != null)
                .filter(entity -> entity.getAnalyticInfo().getTiktokAccessToken() != null)
                .filter(entity -> entity.getAnalyticInfo().getTiktokEventSourceId() != null)
                .forEach(entity ->
                        tiktokDataMap.put(entity.getDomainBot().toLowerCase(),
                                new TiktokData(
                                        entity.getAnalyticInfo().getTiktokAccessToken(),
                                        entity.getAnalyticInfo().getTiktokEventSourceId())
                        )
                );
    }

    private record TiktokData(String accessToken, String eventSourceId) {
    }
}
