package com.example.evatgbotmanager.dto;

import java.util.Map;

public record AnalyticMetaData(
        String sourceDomain,
        String eventType,
        String clickId,
        String traderId,
        String telegramUserId,
        String telegramUserName,

        String deviceId,
        Map<String, String> telegramParams,
        Map<String, String> payload
) {
}
