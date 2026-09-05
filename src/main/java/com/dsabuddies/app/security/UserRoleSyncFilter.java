package com.dsabuddies.app.security;

import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
import com.dsabuddies.app.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRoleSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = null;
            if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                email = oauth2User.getAttribute("email");
                if (email == null) {
                    email = oauth2User.getName();
                }
            } else {
                email = auth.getName();
            }

            if (email != null && !email.isBlank()) {
                String cleanEmail = email.trim().toLowerCase();
                boolean isPrimaryAdmin = UserService.isPrimaryAdmin(cleanEmail);

                Optional<User> userOpt = userRepository.findByEmailIgnoreCase(cleanEmail);
                boolean hasAdminInDb = userOpt.map(u -> "ROLE_ADMIN".equals(u.getRole())).orElse(false);

                boolean shouldBeAdmin = isPrimaryAdmin || hasAdminInDb;

                // Sync DB if primary admin is not marked ROLE_ADMIN in DB
                if (isPrimaryAdmin && userOpt.isPresent() && !"ROLE_ADMIN".equals(userOpt.get().getRole())) {
                    User u = userOpt.get();
                    u.setRole("ROLE_ADMIN");
                    userRepository.save(u);
                }

                boolean hasAdminAuthority = auth.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

                if (shouldBeAdmin && !hasAdminAuthority) {
                    log.info("Synchronizing and elevating session authority for admin user: {}", cleanEmail);
                    Set<GrantedAuthority> updatedAuthorities = new HashSet<>(auth.getAuthorities());
                    updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    updatedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));
                    updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                    Authentication newAuth;
                    if (auth instanceof OAuth2AuthenticationToken oauthToken) {
                        newAuth = new OAuth2AuthenticationToken(
                                oauthToken.getPrincipal(),
                                updatedAuthorities,
                                oauthToken.getAuthorizedClientRegistrationId()
                        );
                    } else {
                        newAuth = new UsernamePasswordAuthenticationToken(
                                auth.getPrincipal(),
                                auth.getCredentials(),
                                updatedAuthorities
                        );
                    }

                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.setAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                SecurityContextHolder.getContext()
                        );
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
