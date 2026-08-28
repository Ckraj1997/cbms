package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.Beneficiary;

public interface BeneficiaryService {

    void addBeneficiary(Beneficiary beneficiary, String username);

    List<Beneficiary> getBeneficiaries(String username);

    long countPending();

    List<Beneficiary> findApprovedByUsername(String username);

    List<Beneficiary> getBeneficiariesByStatus(String pending);

    void updateBeneficiaryStatus(Long id, String pending_admin);

}
