package mca.fincorebanking.service;

import jakarta.annotation.PostConstruct;

public interface TellerDrawerService {

    @PostConstruct
    public void initializeDrawer();

    public Double getCurrentCash();

    public void processCash(String transactionType, Double amount);
    
}
