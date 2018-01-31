package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class PatternMatchImpl implements PatternMatch {

    private final ClassDescription description;
    private final String methodName;

    public PatternMatchImpl(Unit unit, String methodName) {
        description = new ClassDescription(unit.getClassName(), unit.getPackageName(), unit.getCu().getImports(), unit.getModulePath());
        this.methodName = methodName;
    }

    public ClassDescription description() {
        return description;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
