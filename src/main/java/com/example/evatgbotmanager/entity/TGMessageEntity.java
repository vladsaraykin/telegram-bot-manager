package com.example.evatgbotmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "tg_message")
public class TGMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String msg;
    private String i18n;

    @ManyToOne
    @JoinColumn(name = "telegram_bot_id", nullable = false)
    private TelegramBotEntity telegramBot;

    public String getCodKey() {
        return telegramBot.getId() + "_" + code;
    }
}
