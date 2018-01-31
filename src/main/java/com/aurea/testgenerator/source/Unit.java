package com.aurea.testgenerator.source;

import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.file.Path;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Unit {

    private final CompilationUnit cu;
    private final String className;
    private final String packageName;
    private final Path modulePath;

    public Unit(CompilationUnit cu, String className, String packageName, Path modulePath) {
        this.cu = cu;
        this.className = className;
        this.packageName = packageName;
        this.modulePath = modulePath;
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public String getClassName() {
        return className;
    }

    public String fullName() {
        return getPackageName() + "." + getClassName();
    }

    public String getPackageName() {
        return packageName;
    }

    public Path getModulePath() {
        return modulePath;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("fullName", fullName());
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Unit unit = (Unit) o;

        return new EqualsBuilder()
                .append(className, unit.className)
                .append(packageName, unit.packageName)
                .append(modulePath, unit.modulePath)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(className)
                .append(packageName)
                .append(modulePath)
                .toHashCode();
    }
}
