package com.example.evatgbotmanager.repository;

import com.example.evatgbotmanager.entity.TelegramBotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramRepository extends JpaRepository<TelegramBotEntity, Integer> {
    TelegramBotEntity findByDomainBot(String domainBot);
}
