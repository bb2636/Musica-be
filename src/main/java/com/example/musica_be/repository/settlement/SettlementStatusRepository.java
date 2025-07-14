package com.example.musica_be.repository.settlement;

import com.example.musica_be.domain.settlement.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementStatusRepository extends JpaRepository<SettlementStatus, Integer> {
    Optional<SettlementStatus> findByName(String name);
}
