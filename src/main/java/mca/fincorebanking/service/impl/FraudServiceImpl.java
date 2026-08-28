package mca.fincorebanking.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import mca.fincorebanking.entity.FraudLog;
import mca.fincorebanking.repository.FraudLogRepository;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.FraudService;
import mca.fincorebanking.service.NotificationService;

@Service
public class FraudServiceImpl implements FraudService {

    private final FraudLogRepository fraudLogRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public FraudServiceImpl(FraudLogRepository fraudLogRepository, NotificationService notificationService, UserRepository userRepository) {
        this.fraudLogRepository = fraudLogRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Override
    public void logFraud(String username, String reason) {
        FraudLog log = new FraudLog();
        log.setUsername(username);
        log.setReason(reason);
        log.setDetectedAt(LocalDateTime.now());

        notificationService.notify(userRepository.findByUsername(username).get(), "You have logged as fraud due to " + reason);

        fraudLogRepository.save(log);
    }

    @Override
    public long countFrauds() {
        return fraudLogRepository.count();
    }

    @Override
    public List<FraudLog> getAllFraudLogs() {
        return fraudLogRepository.findAll();
    }

}
