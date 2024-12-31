package com.example.evatgbotmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "telegram_bot")
public class TelegramBotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String domainBot;
    private String token;
    private String url;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "analytic_id", referencedColumnName = "id")
    private AnalyticInfoEntity analyticInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "partner_postback_mapping_id", referencedColumnName = "id")
    private PartnerPostBackMappingEntity partnerPostbackMapping;


    @OneToMany(mappedBy = "telegramBot", cascade = CascadeType.ALL)
    private List<TGMessageEntity> messages;
}
