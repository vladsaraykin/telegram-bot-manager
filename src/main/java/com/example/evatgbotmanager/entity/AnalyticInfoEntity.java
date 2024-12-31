package com.example.evatgbotmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "analytic_info")
public class AnalyticInfoEntity {
    //todo refactoring
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String apiKey; // amplitude

    private String tiktokAccessToken;
    private String tiktokEventSourceId;

    private String facebookAccessToken;
    private String facebookPixelId;
    @OneToOne(mappedBy = "analyticInfo")
    private TelegramBotEntity telegramBot;

}
