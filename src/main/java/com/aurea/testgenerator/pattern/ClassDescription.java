package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import java.nio.file.Path;
import java.util.List;
import one.util.streamex.StreamEx;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ClassDescription {

    private final String className;
    private final String packageName;
    private final List<String> imports;
    private final Path modulePath;

    public ClassDescription(String className, String packageName, NodeList<ImportDeclaration> imports, Path modulePath) {
        this.className = className;
        this.packageName = packageName;
        this.imports = StreamEx.of(imports).map(ImportDeclaration::getNameAsString).toList();
        this.modulePath = modulePath;
    }

    public ClassDescription(Unit unit) {
        this.className = unit.getClassName();
        this.packageName = unit.getPackageName();
        this.imports = StreamEx.of(unit.getCu().getImports()).map(ImportDeclaration::getNameAsString).toList();
        this.modulePath = unit.getModulePath();
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<String> getImports() {
        return imports;
    }

    public Path getModulePath() {
        return modulePath;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
