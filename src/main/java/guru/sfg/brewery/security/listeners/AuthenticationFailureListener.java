package guru.sfg.brewery.security.listeners;

import guru.sfg.brewery.domain.security.LoginFailure;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.LoginFailureRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthenticationFailureListener {

    private final LoginFailureRepository loginFailureRepository;
    private final UserRepository userRepository;

    @EventListener
    public void listen(AuthenticationFailureBadCredentialsEvent event) {
        log.debug("User bad cred");
        if(event.getSource() instanceof UsernamePasswordAuthenticationToken) {
            LoginFailure.LoginFailureBuilder builder = LoginFailure.builder();
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)event.getSource();
            if(token.getPrincipal()instanceof String){
                String userName = (String)token.getPrincipal();
                builder.username(userName);
                log.debug("Attempted username: " +userName);
                userRepository.findByUsername(userName).ifPresent(builder::user);
            }
            if(token.getDetails() instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails details = (WebAuthenticationDetails)token.getDetails();
                log.debug("Source IP: " + details.getRemoteAddress());
                builder.sourceIp(details.getRemoteAddress());
            }
            LoginFailure failure = loginFailureRepository.save(builder.build());
            log.debug("Failure event: " + failure.getId());

            if(failure.getUser()!=null){
                lockUserAccount(failure.getUser());
            }
        }
    }

    private void lockUserAccount(User user) {
        List<LoginFailure> failures = this.loginFailureRepository.findAllByUserAndCreatedDateIsAfter(user,
                Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        if(failures.size()>3){
            log.debug("Locking user account .... ");
            user.setAccountNonLocked(false);
            this.userRepository.save(user);
        }
    }
}
