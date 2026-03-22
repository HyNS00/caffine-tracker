package com.hyuns.cafit.context;

import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import javax.sql.DataSource;

public class CleanupExecutionListener extends AbstractTestExecutionListener implements Ordered {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        DataSource dataSource = testContext.getApplicationContext().getBean(DataSource.class);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/cleanup.sql"));
        populator.execute(dataSource);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
