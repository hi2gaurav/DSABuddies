package com.dsabuddies.app.security;

import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ADMIN_EMAIL = "hi2gauravgb@gmail.com";

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getName();
        }

        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String finalEmail = email.trim().toLowerCase();
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(finalEmail);
        User user;

        String expectedRole = UserService.isPrimaryAdmin(finalEmail) ? "ROLE_ADMIN" : "ROLE_USER";

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(name);
            user.setAvatarUrl(picture);
            // Strict enforcement: primary admin is granted ROLE_ADMIN
            user.setRole(expectedRole);
        } else {
            user = User.builder()
                    .email(finalEmail)
                    .name(name)
                    .avatarUrl(picture)
                    .role(expectedRole)
                    .build();
        }

        userService.updateStreak(user); // saves user

        java.util.Set<org.springframework.security.core.GrantedAuthority> authorities = new java.util.HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        if ("ROLE_ADMIN".equals(user.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
