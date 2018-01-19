package com.aurea.testgenerator.config;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.pattern.PatternMatcher;
import com.aurea.testgenerator.pattern.SandboxMatcher;
import com.aurea.testgenerator.template.MatchCollector;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.lineSeparator;

@Configuration
public class MethodRunConfig extends SingleModuleConfig {
    @Override
    protected Path src() {
        return Paths.get("Sandbox");
    }

    @Override
    protected PatternMatcher patternMatcher() {
        return new SandboxMatcher();
    }

    @Override
    protected MatchCollector collector() {
        return classesToMatches -> {
            StringBuilder builder = new StringBuilder();
            for (ClassDescription cd : classesToMatches.keySet()) {
                List<PatternMatch> matches = classesToMatches.get(cd);
                builder.append(lineSeparator())
                        .append("Matches in class: ")
                        .append(cd.getPackageName())
                        .append(".")
                        .append(cd.getClassName());
                for (PatternMatch pm : matches) {
                    builder.append("\t").append(pm).append(lineSeparator());
                }
            }
            System.out.println(builder.toString());
        };
    }
}
