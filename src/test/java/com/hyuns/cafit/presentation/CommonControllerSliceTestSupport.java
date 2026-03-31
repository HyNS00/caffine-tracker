package com.hyuns.cafit.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyuns.cafit.context.ResetMockTestExecutionListener;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import com.hyuns.cafit.global.config.SecurityConfig;
import com.hyuns.cafit.global.security.LoginUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@Import({LoginUserArgumentResolver.class, SecurityConfig.class})
@TestExecutionListeners(
    value = ResetMockTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
public abstract class CommonControllerSliceTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected UserRepository userRepository;

    protected static final long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpLoginUser() {
        User testUser = createTestUser();
        given(userRepository.findById(TEST_USER_ID))
            .willReturn(Optional.of(testUser));
    }

    protected MockHttpSession loginSession() {
        return loginSession(TEST_USER_ID);
    }

    protected MockHttpSession loginSession(long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    private User createTestUser() {
        User user = new User("test@example.com", "encodedPassword", "테스트유저");
        ReflectionTestUtils.setField(user, "id", TEST_USER_ID);
        return user;
    }
}
