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
import java.util.Optional;

@Slf4j
public class FacebookAnalyticService implements AnalyticService {
    private final WebClient webClient;
    private final TelegramRepository telegramRepository;

    private final Map<String, FacebookData> facebookDataMap = new HashMap<>();

    public FacebookAnalyticService(WebClient webClient, TelegramRepository telegramRepository) {
        this.webClient = webClient;
        this.telegramRepository = telegramRepository;
    }
    @Override
    public Mono<String> sendEvent(AnalyticMetaData analyticMetaData) {
        FacebookData facebookData = facebookDataMap.get(analyticMetaData.sourceDomain());
        if (StringUtils.isBlank(analyticMetaData.sourceDomain()) || facebookData == null) {
            log.warn("API key not registered {}", analyticMetaData.sourceDomain());
            return Mono.empty();
        }

        var body = new HashMap<String, Object>();
        var data = new HashMap<String, Object>();
        body.put("data", List.of(data));
        data.put("event_name", analyticMetaData.eventType());
        data.put("event_time", Instant.now().getEpochSecond());
        data.put("action_source", "website");
        data.put("user_data", Map.of("external_id", List.of(
                Optional.ofNullable(analyticMetaData.telegramUserId())
                        .or(() -> Optional.ofNullable(analyticMetaData.traderId()))
                        .orElse("undefined")

        )));

        return webClient.post()
                .uri("https://graph.facebook.com/v20.0/{PIXEL_ID}/events", uriBuilder ->
                    uriBuilder
                            .queryParam("access_token", facebookData.accessToken)
                            .build(facebookData.pixelId)
                )
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .bodyValue(body)
                .exchangeToMono(s -> {
                    HttpStatusCode httpStatusCode = s.statusCode();
                    if (httpStatusCode.isError()) {
                        log.error("Failed send event analytic for domain {}", analyticMetaData.sourceDomain());
                    }
                    return s.bodyToMono(new ParameterizedTypeReference<byte[]>() {}).map(e -> new String(e, StandardCharsets.UTF_8));
                })
                .doOnSuccess(s -> log.info("{} Response from facebook {}", analyticMetaData.sourceDomain(), s));
    }

    @Override
    @Scheduled(fixedRate = 120_000)
    public void invalidateCache() {
        telegramRepository.findAll()
                .stream()
                .filter(entity -> entity.getAnalyticInfo() != null)
                .filter(entity -> entity.getAnalyticInfo().getFacebookAccessToken() != null)
                .filter(entity -> entity.getAnalyticInfo().getFacebookPixelId() != null)
                .forEach(entity ->
                    facebookDataMap.put(entity.getDomainBot().toLowerCase(),
                            new FacebookData(
                                    entity.getAnalyticInfo().getFacebookAccessToken(),
                                    entity.getAnalyticInfo().getFacebookPixelId()
                            )
                    )
                );
    }

    private record FacebookData(String accessToken, String pixelId){}
}
