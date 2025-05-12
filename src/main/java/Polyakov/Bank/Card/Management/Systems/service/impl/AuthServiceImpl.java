package Polyakov.Bank.Card.Management.Systems.service.impl;

import Polyakov.Bank.Card.Management.Systems.exception.BadRequestException;
import Polyakov.Bank.Card.Management.Systems.exception.ResourceNotFoundException;
import Polyakov.Bank.Card.Management.Systems.model.constant.RoleType;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.RegisterRequest;
import Polyakov.Bank.Card.Management.Systems.model.entity.Role;
import Polyakov.Bank.Card.Management.Systems.model.entity.User;
import Polyakov.Bank.Card.Management.Systems.repository.RoleRepository;
import Polyakov.Bank.Card.Management.Systems.repository.UserRepository;
import Polyakov.Bank.Card.Management.Systems.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();
        logger.info("Registration attempt for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed: Email {} already exists.", email);
            throw new BadRequestException("Email '" + email + "' is already taken!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> {
                    logger.error("Default role ROLE_USER not found in database!");
                    return new ResourceNotFoundException("Default role ROLE_USER not found.");
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("User registered successfully with email: {}", email);
    }
}
