package com.example.evatgbotmanager.service;

import com.example.evatgbotmanager.dto.AnalyticMetaData;
import reactor.core.publisher.Mono;

public interface AnalyticService {

    Mono<String> sendEvent(AnalyticMetaData analyticMetaData);

    void invalidateCache();
}
