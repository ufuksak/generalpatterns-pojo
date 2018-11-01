package com.aurea.testgenerator.generation.patterns.builder

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator

class BuilderTestGeneratorCompatibleGetterTypeSpec extends MatcherPipelineTest {

    def "Builder cases test for bonus: compatible getter type"() {
        expect:
        onClassCodeExpect """
            public class Person {

                private String firstName;
                private String lastName;
            
                public String getFirstName() {
                    return firstName;
                }
            
                public void setFirstName(String firstName) {
                    this.firstName = firstName;
                }
            
                public String getLastName() {
                    return lastName;
                }
            
                public void setLastName(String lastName) {
                    this.lastName = lastName;
                }
            }
            
            public class Employee extends Person {

                private long id;
                private Boolean active;
            
                public long getId() {
                    return id;
                }
            
                public void setId(long id) {
                    this.id = id;
                }
            
                public Boolean isActive() {
                    return active;
                }
            
                public void setActive(Boolean active) {
                    this.active = active;
                }
            }
            
            public class EmployeeBuilder {

                private String firstName;
                private String lastName;
                private long id;
                private Boolean active;
            
                public EmployeeBuilder withId(long id) {
                    this.id = id;
                    return this;
                }
            
                public EmployeeBuilder withActive(Boolean active) {
                    this.active = active;
                    return  this;
                }
            
                public EmployeeBuilder withFirstName(String firstName) {
                    this.firstName = firstName;
                    return this;
                }
            
                public EmployeeBuilder withLastName(Long value) {
                    return this;
                }
            
                public Employee build() {
                    Employee employee = new Employee();
                    employee.setActive(active);
                    employee.setId(id);
                    employee.setFirstName(firstName);
                    return employee;
                }
            }

        """, """
            package sample;

            import javax.annotation.Generated;
            import org.junit.Test;
            import static org.junit.Assert.assertEquals;
            
            @Generated("GeneralPatterns")
            public class FooPatternTest {
            
                @Test
                public void testBuildWithId() {
                    // Arrange
                    long testData = 10L;
                    EmployeeBuilder builder = new EmployeeBuilder();
                    builder.withId(testData);
                    // Act
                    Employee pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getId());
                }
            
                @Test
                public void testBuildWithActive() {
                    // Arrange
                    Boolean testData = true;
                    EmployeeBuilder builder = new EmployeeBuilder();
                    builder.withActive(testData);
                    // Act
                    Employee pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.isActive());
                }
            
                @Test
                public void testBuildWithFirstName() {
                    // Arrange
                    String testData = "testData";
                    EmployeeBuilder builder = new EmployeeBuilder();
                    builder.withFirstName(testData);
                    // Act
                    Employee pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getFirstName());
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new BuilderTestGenerator(reporter, visitReporter)
    }
}
