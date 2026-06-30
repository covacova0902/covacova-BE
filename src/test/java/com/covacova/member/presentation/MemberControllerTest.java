package com.covacova.member.presentation;

import com.covacova.global.security.SecurityConfig;
import com.covacova.member.application.MemberService;
import com.covacova.member.exception.DuplicateEmailException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 정상_회원가입_요청이면_200과_memberId를_반환한다() throws Exception {
        given(memberService.signup(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .willReturn(1L);

        //테스트용 요청 데이터 생성
        SignupRequest request = new SignupRequest("test@example.com", "password123", true, true);

        //POST /api/members로 JSON 요청 전송
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1));
    }

    @Test
    void 이메일_형식이_잘못되면_400을_반환한다() throws Exception {

        SignupRequest request = new SignupRequest("not-an-email", "password123", true, true);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void 비밀번호가_8자_미만이면_400을_반환한다() throws Exception {

        SignupRequest request = new SignupRequest("test@example.com", "short1", true, true);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void termsAgreed가_false이면_400을_반환한다() throws Exception {

        SignupRequest request = new SignupRequest("test@example.com", "password123", false, true);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void privacyAgreed가_false이면_400을_반환한다() throws Exception {

        SignupRequest request = new SignupRequest("test@example.com", "password123", true, false);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void 이메일이_중복이면_409를_반환한다() throws Exception {
        given(memberService.signup(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .willThrow(new DuplicateEmailException("test@example.com"));

        SignupRequest request = new SignupRequest("test@example.com", "password123", true, true);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
