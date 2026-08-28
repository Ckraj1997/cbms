package mca.fincorebanking.security;

import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import mca.fincorebanking.entity.User;
import mca.fincorebanking.repository.UserRepository;
import mca.fincorebanking.service.FraudService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FraudService fraudService;

    public CustomUserDetailsService(UserRepository userRepository, FraudService fraudService) {
        this.userRepository = userRepository;
        this.fraudService = fraudService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isActive()) {
            throw new DisabledException("User blocked");
        }

        if (user.getFailedAttempts() >= 3) {
            fraudService.logFraud(
                    user.getUsername(),
                    "Multiple failed login attempts");
            user.setActive(false);
            userRepository.save(user);
            throw new DisabledException("Account blocked due to suspicious login");
        }

        var authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        System.out.println("LOGIN USER = " + user.getUsername());
        System.out.println("ROLE FROM DB = " + user.getRole());
        System.out.println("AUTHORITIES = " + authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole())));

    }

}
