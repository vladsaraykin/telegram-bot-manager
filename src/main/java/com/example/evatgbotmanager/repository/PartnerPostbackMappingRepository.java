package com.example.evatgbotmanager.repository;

import com.example.evatgbotmanager.entity.PartnerPostBackMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerPostbackMappingRepository extends JpaRepository<PartnerPostBackMappingEntity, Integer> {
}
