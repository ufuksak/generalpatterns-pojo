//package com.aurea.testgenerator.value.random;
//
//import com.github.javaparser.ast.pattern.PrimitiveType;
//import junitparams.JUnitParamsRunner;
//import junitparams.Parameters;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@RunWith(JUnitParamsRunner.class)
//public class TestValueExpressionFactoryTest {
//
//    @Test
//    @Parameters({
//            "List<Foo>|singletonList(pojoFactory.manufacturePojo(Foo.class))",
//            "Collection<Foo>|singletonList(pojoFactory.manufacturePojo(Foo.class))",
//            "Foo|pojoFactory.manufacturePojo(Foo.class)"})
//    public void getReturnExpectedForGivenListOfPojo(String pattern, String expected) throws Exception {
//        RandomValueFactory factory = new RandomValueFactory();
//
//        String result = factory.get(pattern).get();
//
//        assertThat(result).isEqualTo(expected);
//    }
//
//    @Test
//    public void getReturnExpectedForPrimitive() throws Exception {
//        RandomValueFactory factory = new RandomValueFactory();
//
//        String result = factory.get(PrimitiveType.Primitive.INT.name()).get();
//
//        Integer.parseInt(result);
//    }
//
//}