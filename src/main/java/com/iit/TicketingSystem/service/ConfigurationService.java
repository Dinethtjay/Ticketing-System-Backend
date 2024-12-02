package com.iit.TicketingSystem.service;

import com.iit.TicketingSystem.exception.ConcurrentUpdateException;
import com.iit.TicketingSystem.model.Configuration;
import com.iit.TicketingSystem.repository.ConfigurationRepository;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigurationService {

    @Autowired
    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Configuration saveConfiguration(Configuration configuration) {
//        return configurationRepository.save(configuration);
        try {
            return configurationRepository.save(configuration);
        } catch (OptimisticLockException | StaleObjectStateException e) {
            throw new ConcurrentUpdateException("Configuration was updated by another transaction. Please try again.",e);
        }
    }

    public Optional<Configuration> loadLastConfiguration() {

        return configurationRepository.findTopByOrderByIdDesc();
    }
}
