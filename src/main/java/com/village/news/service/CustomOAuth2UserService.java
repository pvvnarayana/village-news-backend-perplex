package com.village.news.service;

import com.village.news.entity.User;
import com.village.news.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User       = delegate.loadUser(userRequest);

        String googleId             = oAuth2User.getAttribute("sub");
        String email                = oAuth2User.getAttribute("email");
        String name                 = oAuth2User.getAttribute("name");
        String picture              = oAuth2User.getAttribute("picture");

        // create or update local user
        User user = userRepository.findByGoogleId(googleId)
                       .orElse(new User());
        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setProfilePictureUrl(picture);
        userRepository.save(user);

        return oAuth2User;
    }
}
