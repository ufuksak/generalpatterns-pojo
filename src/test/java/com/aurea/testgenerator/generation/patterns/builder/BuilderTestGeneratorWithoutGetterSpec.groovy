package com.aurea.testgenerator.generation.patterns.builder

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator

class BuilderTestGeneratorWithoutGetterSpec extends MatcherPipelineTest {

    def "Builder cases test assignment 3"() {
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
                private boolean active;
            
                public long getId() {
                    return id;
                }
            
                public void setId(long id) {
                    this.id = id;
                }
            
                public boolean getActive() {
                    return active;
                }
            
                public void setActive(boolean active) {
                    this.active = active;
                }
            }
            
            public class EmployeeBuilder {

                private String firstName;
                private String lastName;
                private long id;
                private boolean active;
            
                public EmployeeBuilder withId(long id) {
                    this.id = id;
                    return this;
                }
            
                public EmployeeBuilder withActive(boolean active) {
                    this.active = active;
                    return  this;
                }
            
                public EmployeeBuilder withFirstName(String firstName) {
                    this.firstName = firstName;
                    return this;
                }
            
                public EmployeeBuilder withLastName(String lastName) {
                    this.lastName = lastName;
                    return this;
                }
            
                public EmployeeBuilder nonExistingInPojo(String value) {
                    return this;
                }
            
                public Employee build() {
                    Employee employee = new Employee();
                    employee.setActive(active);
                    employee.setId(id);
                    employee.setFirstName(firstName);
                    employee.setLastName(lastName);
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
                    boolean testData = true;
                    EmployeeBuilder builder = new EmployeeBuilder();
                    builder.withActive(testData);
                    // Act
                    Employee pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getActive());
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
            
                @Test
                public void testBuildWithLastName() {
                    // Arrange
                    String testData = "testData";
                    EmployeeBuilder builder = new EmployeeBuilder();
                    builder.withLastName(testData);
                    // Act
                    Employee pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getLastName());
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new BuilderTestGenerator(reporter, visitReporter)
    }
}
