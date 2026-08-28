package mca.fincorebanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mca.fincorebanking.entity.Role;
import mca.fincorebanking.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    long countByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Add this search query
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchCustomers(@Param("keyword") String keyword);

    public List<User> findByRole(String role_customer);

}
