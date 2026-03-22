package com.hyuns.cafit.context.security;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

public class WithLoginUserExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        WithLoginUser annotation = findAnnotation(context);
        if (annotation == null) {
            return;
        }

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", annotation.userId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RequestContextHolder.resetRequestAttributes();
    }

    private WithLoginUser findAnnotation(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        WithLoginUser annotation = method.getAnnotation(WithLoginUser.class);
        if (annotation != null) {
            return annotation;
        }
        return context.getRequiredTestClass().getAnnotation(WithLoginUser.class);
    }
}
