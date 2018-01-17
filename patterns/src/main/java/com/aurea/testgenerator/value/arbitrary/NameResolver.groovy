package com.aurea.testgenerator.value.arbitrary

import org.apache.commons.lang.StringUtils


class NameResolver {

    Map<String, Integer> alreadyUsedNames = new HashMap<>()
    String lastResolved

    String nextName(String type) {
        nextName(type, false)
    }

    String nextName(String type, boolean plural) {
        if (!alreadyUsedNames.containsKey(type)) {
            alreadyUsedNames.put(type, 1)
            lastResolved = typeAsName(type, plural)
        } else {
            Integer index = alreadyUsedNames.computeIfPresent(type, { t, lastIndex -> ++lastIndex })
            lastResolved = typeAsName(type, plural) + index
        }
        return lastResolved
    }

    String lastResolved() {
        lastResolved
    }

    String typeAsName(String type, boolean plural) {
        StringUtils.uncapitalize(type) + (plural ? "s" : "")
    }
}
