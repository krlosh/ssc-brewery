package guru.sfg.brewery.bootstrap;

import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.AuthorityRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDataLoader implements CommandLineRunner {

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        loadSecurityData();
    }

    private void loadSecurityData() {
        Authority adminRole = Authority.builder().permission("ROLE_ADMIN").build();
        Authority userRole = Authority.builder().permission("ROLE_USER").build();
        Authority customerRole = Authority.builder().permission("ROLE_CUSTOMER").build();
        if (authorityRepository.count()==0) {
            authorityRepository.save(adminRole);
            authorityRepository.save(userRole);
            authorityRepository.save(customerRole);

        }

        if(!userRepository.findByUsername("spring").isPresent()) {
            User spring = User.builder().username("spring").password(passwordEncoder.encode("guru")).authority(adminRole).build();
            userRepository.save(spring);
        }

        if(!userRepository.findByUsername("user").isPresent()) {
            User user = User.builder().username("user").password(passwordEncoder.encode("password")).authority(userRole).build();
            userRepository.save(user);
        }

        if(!userRepository.findByUsername("scott").isPresent()) {
            User scott = User.builder().username("scott").password(passwordEncoder.encode("tiger")).authority(customerRole).build();
            userRepository.save(scott);
        }
        log.debug("Users loaded {}",userRepository.count());
    }

}
