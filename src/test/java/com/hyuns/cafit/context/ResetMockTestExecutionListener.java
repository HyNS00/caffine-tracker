package com.hyuns.cafit.context;

import org.mockito.internal.util.MockUtil;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class ResetMockTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        for (String beanName : testContext.getApplicationContext().getBeanDefinitionNames()) {
            Object bean = testContext.getApplicationContext().getBean(beanName);
            if (MockUtil.isMock(bean)) {
                MockUtil.resetMock(bean);
            }
        }
    }
}
