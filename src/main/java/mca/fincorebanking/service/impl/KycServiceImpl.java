package mca.fincorebanking.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import mca.fincorebanking.entity.KycDocument;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.KycRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.NotificationService;

@Service
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private Path fileStorageLocation;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public KycServiceImpl(KycRepository kycRepository, UserRepository userRepository, NotificationService notificationService) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir)
                    .toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public KycDocument uploadKycDocument(Long userId, String docType, String docNumber, MultipartFile file) {

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new RuntimeException("Invalid path sequence " + originalFileName);
        }

        try {

            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }

            String newFileName = "user_" + userId + "_" + docType.toLowerCase() + "_" + UUID.randomUUID()
                    + fileExtension;

            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            KycDocument kyc = new KycDocument();
            kyc.setUser(user);
            kyc.setDocumentType(docType);
            kyc.setDocumentNumber(docNumber);
            kyc.setFileName(newFileName);
            kyc.setFilePath(targetLocation.toString());
            kyc.setStatus("PENDING");
            kyc.setUploadDate(LocalDateTime.now());

            notificationService.notify(user, "Document for Kyc succefully submitted");

            return kycRepository.save(kyc);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    @Override
    public Optional<KycDocument> getKycByUser(User user) {
        return kycRepository.findByUser(user);
    }

    @Override
    public List<KycDocument> getPendingKycs() {
        return kycRepository.findByStatus("PENDING");
    }

    @Override
    public void updateKycStatus(Long kycId, String status) {
        KycDocument kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC Document not found"));

        kyc.setStatus(status);
        kyc.setVerifiedDate(LocalDateTime.now());
        kycRepository.save(kyc);
        notificationService.notify(kyc.getUser(), "Kyc status Updated");
    }

    @Override
    public List<KycDocument> getKycsByStatus(String pending) {
        return kycRepository.findByStatus(pending);
    }
}