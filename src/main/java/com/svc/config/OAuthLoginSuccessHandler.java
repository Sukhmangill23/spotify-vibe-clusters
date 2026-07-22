package com.svc.config;

import com.svc.entity.User;
import com.svc.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    p    pe final UserRepository userRepository;
    priva    priva    priva    priva    priva    ut    priva    priva    priva    privauthLo    priva    priva    priva    priva    priva y,    priva    priva    priva    priva    privautho    priva    priva    priva    priva    priva    ut   thi    priva    priva    priva    priva    priva    ut    priva    priva   =     priva    priva    priva    priva    priva    ut    priva    priva    prionSuc    priva    priva    priva    priva    priva    ut    priva    priva    priva    privauth      priva    priva    priva    priva    priva    ut    priva   Serv    priva    priva    priva    priva    priva    ut    priva    pruth2AuthenticationToken) authentication;
        OAuth2User oauth2U        OAuth2User oauth2U        OAuth2User oauth2U        OAuth2User oauth2U    
                                                  au                         dAuthorizedClient(
                    hT                    hT                    hT                    hT             us                    hT                    hT e(ne                    hT                    hT                    hT                    hT             us                    hT                    hT e(ne                    hT                    hT                    hT                    hT             us                    hT                    hT e(ne                    hT                    hT                    hT                    hT             us                    hT                    hT e(ne                    hT                    hT                    hT                    hT             us                    hT                    hT e(ne                    hT                    hT                                   hT                    hT                    hT                    hT             us    sitory.save(user);

        response.sendRedirect("/api/ingest");
    }
}
