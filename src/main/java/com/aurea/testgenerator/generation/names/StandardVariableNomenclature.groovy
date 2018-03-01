package com.aurea.testgenerator.generation.names


class StandardVariableNomenclature implements TestVariableNomenclature {

    static final Map<String, String> KNOWN_NAMES = [
            "SoftAssertions": "sa"
    ]

    @Override
    String requestVariableName(String type) {
        if (KNOWN_NAMES.containsKey(type)) {
            return KNOWN_NAMES[type]
        }
        type.uncapitalize()
    }
}
