package mca.fincorebanking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import mca.fincorebanking.entity.KycDocument;
import mca.fincorebanking.entity.User;

public interface KycService {
    KycDocument uploadKycDocument(Long userId, String docType, String docNumber, MultipartFile file);

    Optional<KycDocument> getKycByUser(User user);

    List<KycDocument> getPendingKycs();

    void updateKycStatus(Long kycId, String status);

    List<KycDocument> getKycsByStatus(String pending);
}