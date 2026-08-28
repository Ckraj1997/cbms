package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.Notification;
import mca.fincorebanking.entity.User;

public interface NotificationService {

    List<Notification> getUserNotifications(String username);

    void notify(User user, String message);
}
