package guru.sfg.brewery.security.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationFailureListener {

    @EventListener
    public void listen(AuthenticationFailureBadCredentialsEvent event) {
        log.debug("User bad cred");
        if(event.getSource() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)event.getSource();
            if(token.getPrincipal()instanceof String){
                String userName = (String)token.getPrincipal();
                log.debug("Username logged in: " +userName);
            }
            if(token.getDetails() instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails details = (WebAuthenticationDetails)token.getDetails();
                log.debug("Source IP: " + details.getRemoteAddress());
            }
        }
    }
}
