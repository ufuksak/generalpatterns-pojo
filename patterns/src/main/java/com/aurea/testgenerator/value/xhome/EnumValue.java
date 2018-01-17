package com.aurea.testgenerator.value.xhome;

import static com.aurea.testgenerator.source.ParsingUtils.parseSimpleName;

import com.aurea.testgenerator.prescans.projects.redknee.xgen.enums.XGenEnum;
import com.aurea.testgenerator.prescans.projects.redknee.xgen.enums.XGenEnumItem;
import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class EnumValue implements TestValue {

    private final String fullName;
    private final String simpleName;
    private final String value;

    public EnumValue(XGenEnum xGenEnum, XGenEnumItem item) {
        this.fullName = xGenEnum.getFullName();
        this.simpleName = parseSimpleName(fullName);
        this.value = item.getName();
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.of(fullName);
    }

    @Override
    public String get() {
        return simpleName + "." + value;
    }
}
