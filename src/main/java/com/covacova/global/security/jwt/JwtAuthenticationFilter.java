package com.covacova.global.security.jwt;

import com.covacova.global.security.auth.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        //유효한 토큰이 존재할 때 인증 처리 진행
        if (token != null && jwtTokenProvider.validateToken(token)) {
            //토큰 subject에 담긴 memberId 꺼내기
            Long memberId = jwtTokenProvider.getMemberId(token);

            //memberId로 DB 조회해서 UserDetails 생성
            UserDetails userDetails = customUserDetailsService.loadUserByMemberId(memberId);

            //Security가 이해하는 인증 완료 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            //요청의 부가 정보 추가(IP, 세션 ID 등)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //SecurityContextHolder에 인증 정보 등록 -> 여기서부터는 인증된 사용자의 요청
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        //다음 필터로 요청/응답 전달
        filterChain.doFilter(request, response);
    }

    //Bearer을 뗀 순수 토큰 문자열
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
