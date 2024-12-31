package com.example.evatgbotmanager.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterTelegramDto {

    private String token;
    private List<Msg> descriptions;
    private List<Msg> introductions;
    private String url;
    private String domainBot;
    private AnalyticInfo analyticInfo;

    public record AnalyticInfo(String apiKey){}
    public record Msg(String i18n, String msg){}
}
