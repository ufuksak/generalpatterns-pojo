package com.aurea.testgenerator.dao;

import com.aurea.testgenerator.coverage.MethodCoverageCriteria;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodCoverageQueryTest {
    @Test
    public void staticFactoryMethod() throws Exception {
        MethodCoverageCriteria of = MethodCoverageCriteria.of(null, null);

        assertThat(of).isNotNull();
    }
}