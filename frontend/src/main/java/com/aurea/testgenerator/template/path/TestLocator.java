package com.aurea.testgenerator.template.path;

import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.pattern.ClassDescription;

import java.util.Optional;

public interface TestLocator {

    Optional<Unit> get(ClassDescription description);
}
