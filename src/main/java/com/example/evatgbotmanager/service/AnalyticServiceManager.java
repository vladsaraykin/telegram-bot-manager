package com.example.evatgbotmanager.service;

import com.example.evatgbotmanager.dto.AnalyticMetaData;
import com.example.evatgbotmanager.enums.Analytic;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AnalyticServiceManager {

    private final Map<Analytic, AnalyticService> analyticServiceMap;

    public AnalyticServiceManager(Map<Analytic, AnalyticService> analyticServices) {
        this.analyticServiceMap = analyticServices;
    }

    public void sendEvent(AnalyticMetaData analyticMetaData) {
        for (AnalyticService analyticService : analyticServiceMap.values()) {
            analyticService.sendEvent(analyticMetaData).subscribeOn(Schedulers.boundedElastic()).subscribe();
        }
    }
}
