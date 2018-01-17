package com.aurea.testgenerator.template;

import java.util.List;

public interface TestCaseTemplate extends Template {
    List<String> getImports();

    List<String> getPreparedClasses();
}
