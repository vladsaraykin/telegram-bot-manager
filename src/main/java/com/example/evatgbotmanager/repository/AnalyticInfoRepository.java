package com.example.evatgbotmanager.repository;

import com.example.evatgbotmanager.entity.AnalyticInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticInfoRepository extends JpaRepository<AnalyticInfoEntity, Integer> {
}
