package com.covacova.member.presentation;

import com.covacova.member.application.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 정상_회원가입_요청이면_200과_memberId를_반환한다() throws Exception {
        given(memberService.signup(anyString(), anyString(), anyBoolean()))
                .willReturn(1L);

        //테스트용 요청 데이터 생성
        SignupRequest request = new SignupRequest("test@example.com", "password123", true, true);

        mockMvc.perform(post("/"))
    }
}
