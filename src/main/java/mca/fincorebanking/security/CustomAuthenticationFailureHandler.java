package mca.fincorebanking.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mca.fincorebanking.repository.UserRepository;

@Component
public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationFailureHandler(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String username = request.getParameter("username");

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            userRepository.save(user);
        });

        response.sendRedirect("/login?error");
    }
}
