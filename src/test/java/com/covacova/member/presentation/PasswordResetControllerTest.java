package com.covacova.member.presentation;

import com.covacova.global.security.SecurityConfig;
import com.covacova.global.security.auth.CustomUserDetailsService;
import com.covacova.global.security.jwt.JwtTokenProvider;
import com.covacova.member.application.PasswordResetService;
import com.covacova.member.dto.request.PasswordResetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordResetController.class)
@Import(SecurityConfig.class)
public class PasswordResetControllerTest {

    private static final String RESET_REQUEST_MESSAGE = "입력하신 이메일 주소로 인증코드를 보내드렸습니다. " +
            "메일이 오지 않는다면 가입 시 사용한 이메일이 맞는지 확인해주세요.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void 정상_요청이면_200과_고정_메시지를_반환한다() throws Exception {
        doNothing().when(passwordResetService).requestReset(anyString());

        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        mockMvc.perform(post("/api/auth/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value(RESET_REQUEST_MESSAGE));

        verify(passwordResetService).requestReset("test@example.com");
    }

    @Test
    void 이메일_형식이_잘못되면_400을_반환한다() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest("not-an-email");

        mockMvc.perform(post("/api/auth/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void 이메일이_비어있으면_400을_반환한다() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest("");

        mockMvc.perform(post("/api/auth/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
