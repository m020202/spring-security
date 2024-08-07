package com.security1.config.oauth;

import com.security1.Repository.MemberRepository;
import com.security1.config.auth.PrincipalDetails;
import com.security1.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 함수 종료 시 @AuthenticationPrincipal 어노테이션이 만들어진다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println(userRequest.getClientRegistration()); // 어떤 OAuth 로 로그인 했는지
        System.out.println("userRequest: " + userRequest);
        // 구글 로그인 버튼 클릭 -> 구글 로그인창 -> 로그인을 완료 -> code를 리턴 (OAuth-Client 라이브러리가) -> Access Token 요청
        // userRequest 정보 -> loadUser 함수 호출 후 회원 프로필을 받기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 회원가입 강제로 진행해보기
        String provider = userRequest.getClientRegistration().getClientId(); // google
        String providerId = oAuth2User.getAttribute("sub");
        String username = provider + "_" + providerId;
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2User.getAttribute("email");
        String role = "ROLE_USER";

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            member = Member.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(member);
        }
        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }
}
