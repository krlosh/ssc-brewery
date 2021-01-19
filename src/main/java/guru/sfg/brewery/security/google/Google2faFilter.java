package guru.sfg.brewery.security.google;

import guru.sfg.brewery.domain.security.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class Google2faFilter extends GenericFilterBean {
    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private final GoogleAuthenticationFailureHandler googleAuthenticationFailureHandler = new GoogleAuthenticationFailureHandler();
    private final RequestMatcher urlIs2fa = new AntPathRequestMatcher("/user/verify2fa");
    private final RequestMatcher urlResource = new AntPathRequestMatcher("/resources/**");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        StaticResourceRequest.StaticResourceRequestMatcher staticResourceRequestMatcher = PathRequest.toStaticResources().atCommonLocations();
        if (urlIs2fa.matches(httpRequest) || urlResource.matches(httpRequest)
                || staticResourceRequestMatcher.matcher(httpRequest).isMatch()){
            filterChain.doFilter(httpRequest, httpResponse);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && !authenticationTrustResolver.isAnonymous(authentication)) {
            log.debug("Processing 2FA filter");
            if(authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                if (user.getUseGoogle2fa() && user.getGoogle2faRequired()) {
                    log.debug("Google 2FA required");
                    googleAuthenticationFailureHandler.onAuthenticationFailure(httpRequest, httpResponse, null);
                    return ;
                }
            }

        }

        filterChain.doFilter(httpRequest, httpResponse);
    }
}
