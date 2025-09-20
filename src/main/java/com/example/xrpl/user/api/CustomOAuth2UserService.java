package com.example.xrpl.user.api;

import com.example.xrpl.user.domain.User;
import com.example.xrpl.user.infrastructure.UserRepository;
import com.example.xrpl.xrpl.api.XRPLTestWalletService;
import com.example.xrpl.xrpl.api.XRPLService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final XRPLTestWalletService xRPLTestWalletService;
    private final XRPLService xrplService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = findOrCreateUser(attributes);

        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user.getRole(),
                user.getId(),
                user.getXrplAddress(),
                user.getXrplSecret(),
                user.getIsKYC(),
                user.getIssuerAddress()
        );
    }

    private User findOrCreateUser(OAuthAttributes attributes) {
        return userRepository.findByEmail(attributes.getEmail())
                .orElseGet(() -> {
                    XRPLTestWalletService.CreateWalletResponse walletResponse = xRPLTestWalletService.createWallet();
                    User newUser = attributes.toEntity(attributes.getEmail(), walletResponse.address(), walletResponse.secret());
                    User savedUser = userRepository.save(newUser);

                    XRPLService.CredentialCreateParams params = new XRPLService.CredentialCreateParams(
                            savedUser.getXrplAddress(),
                            "KYC",
                            "",
                            365L
                    );

                    XRPLService.CredentialCreateResponse credentialResponse = xrplService.createCredential(params);

                    savedUser.updateIssuerAddress(credentialResponse.issuerAddress());

                    return userRepository.save(savedUser);
                });
    }
}