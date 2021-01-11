package guru.sfg.brewery.security;

import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUnlockService {

    private final UserRepository userRepository;

    @Scheduled(fixedRate = 300000)
    public void unlockAccounts() {
        log.debug("Running unlock accounts");

        List<User> lockedUsers = this.userRepository.findAllByAccountNonLockedAndLastModifiedDateIsBefore(false,
                Timestamp.valueOf(LocalDateTime.now().minusSeconds(30)));

        if(!lockedUsers.isEmpty()) {
            log.debug("Locked accounts found. Unlocking ...");
            lockedUsers.forEach(user -> {user.setAccountNonLocked(true);});
            this.userRepository.saveAll(lockedUsers);
        }
    }
}
