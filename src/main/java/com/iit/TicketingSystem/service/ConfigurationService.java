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

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Configuration saveConfiguration(Configuration configuration) {
        Optional<Configuration> existingConfig = configurationRepository.findTopByOrderByIdDesc();

        if (existingConfig.isPresent()) {
            Configuration configToUpdate = existingConfig.get();
            // Update the existing configuration with new values
            configToUpdate.setTotalTickets(configuration.getTotalTickets());
            configToUpdate.setTicketReleaseRate(configuration.getTicketReleaseRate());
            configToUpdate.setCustomerRetrievalRate(configuration.getCustomerRetrievalRate());
            configToUpdate.setMaxTicketCapacity(configuration.getMaxTicketCapacity());
            configToUpdate.setNumVendors(configuration.getNumVendors());
            configToUpdate.setNumCustomers(configuration.getNumCustomers());

            return configurationRepository.save(configToUpdate);
        } else {
            // Save the configuration as a new record since no existing record was found
            return configurationRepository.save(configuration);
        }
    }

    public Optional<Configuration> loadLastConfiguration() {
        return configurationRepository.findTopByOrderByIdDesc();
    }
}
