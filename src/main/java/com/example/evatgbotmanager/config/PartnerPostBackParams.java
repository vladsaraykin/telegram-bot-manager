package com.example.evatgbotmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "partner.postback-params")
@Configuration
@Getter
@Setter
public class PartnerPostBackParams {
    private String clickId;
    private String eventId;
    private String traderId;
    private String status;
    private String registration;
    private String fistReplenishment;
    private String tgUserId;
}
