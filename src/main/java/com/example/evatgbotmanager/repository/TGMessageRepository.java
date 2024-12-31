package com.example.evatgbotmanager.repository;

import com.example.evatgbotmanager.entity.TGMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TGMessageRepository  extends JpaRepository<TGMessageEntity, Integer> {
}
