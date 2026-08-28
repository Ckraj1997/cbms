package mca.fincorebanking.service.impl;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import mca.fincorebanking.config.NumericConfigStore;
import mca.fincorebanking.service.TellerDrawerService;

@Service
public class TellerDrawerServiceImp implements TellerDrawerService {

    private Double currentCash = 0.0;

    @Override
    @PostConstruct
    @SuppressWarnings("UseSpecificCatch")
    public void initializeDrawer() {
       try {
            // Read the initial baseline value directly from your existing configuration store
            this.currentCash = NumericConfigStore.get().dashboard.tellerCashInDrawer;
            System.out.println("Loaded initial drawer cash from NumericConfigStore: " + this.currentCash);
        } catch (Exception e) {
            System.err.println("Failed to load numeric config. Defaulting to 0.0");
            this.currentCash = 0.0;
        }
    }

    @Override
    public Double getCurrentCash() {
        return this.currentCash;
    }

    @Override
    public synchronized void processCash(String transactionType, Double amount) {
       if (transactionType.equalsIgnoreCase("DEPOSIT")) {
            this.currentCash += amount;
        } else if (transactionType.equalsIgnoreCase("WITHDRAWAL")) {
            if (this.currentCash < amount) {
                throw new RuntimeException("Insufficient cash in the teller drawer!");
            }
            this.currentCash -= amount;
        }
    }

}
