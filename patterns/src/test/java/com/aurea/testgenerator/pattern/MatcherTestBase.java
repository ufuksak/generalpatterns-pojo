package com.aurea.testgenerator.pattern;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MatcherConfig.class)
public class MatcherTestBase<T extends PatternMatcher> {

    @Autowired
    protected T matcher;
}
