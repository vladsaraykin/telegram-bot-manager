package com.example.evatgbotmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "partner_postback_mapping")
public class PartnerPostBackMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(mappedBy = "partnerPostbackMapping", fetch = FetchType.EAGER)
    private TelegramBotEntity telegramBot;
    private String clickId;
    private String eventId;
    private String traderId;
    private String status;
    private String registration;
    private String fistReplenishment;

    private String tgUserId;
}
