package Polyakov.Bank.Card.Management.Systems.service.impl;

import Polyakov.Bank.Card.Management.Systems.model.entity.User;
import Polyakov.Bank.Card.Management.Systems.repository.UserRepository;
import Polyakov.Bank.Card.Management.Systems.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static Polyakov.Bank.Card.Management.Systems.util.ServiceMessagesUtil.USER_NOT_FOUND_WITH_EMAIL;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_EMAIL + email));

        return new SecurityUserDetails(user);
    }
}
