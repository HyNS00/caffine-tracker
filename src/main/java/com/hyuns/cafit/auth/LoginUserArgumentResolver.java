package com.hyuns.cafit.auth;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.UserRepository;
import com.hyuns.cafit.errors.AuthenticationException;
import com.hyuns.cafit.errors.EntityNotFoundException;
import com.hyuns.cafit.errors.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        boolean isUserType = User.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && (isLongType || isUserType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new AuthenticationException(ErrorMessage.UNAUTHORIZED);
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException(ErrorMessage.UNAUTHORIZED);
        }

        // Long 타입 요청 시 → userId 그대로 반환 (DB 조회 없음)
        if (Long.class.isAssignableFrom(parameter.getParameterType())) {
            return userId;
        }

        // User 타입 요청 시 → DB 조회
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.USER_NOT_FOUND));
    }

}
