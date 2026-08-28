package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.User;

public interface UserService {

    void saveUser(User user);

    List<User> getAllUsers();

    long countUsers();

    long countByRole(String role);

    void blockUser(Long userId);

    void unblockUser(Long userId);

    User findByUsername(String username);

    User registerCustomer(User user);
    
    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);

    List<User> searchCustomers(String keyword);
}
