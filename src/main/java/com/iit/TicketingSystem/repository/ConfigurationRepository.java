package com.iit.TicketingSystem.repository;

import com.iit.TicketingSystem.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Optional<Configuration> findTopByOrderByIdDesc();
}
