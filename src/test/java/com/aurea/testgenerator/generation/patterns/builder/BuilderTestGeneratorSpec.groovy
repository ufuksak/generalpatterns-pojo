package com.aurea.testgenerator.generation.patterns.builder

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator

class BuilderTestGeneratorSpec extends MatcherPipelineTest {

    def "Builder cases test assignment 1"() {
        expect:
        onClassCodeExpect """
            import java.util.List;
            
            public class Person {

                private String firstName;
                private String lastName;
                private int numberOfChildren;
                private Long ssn;
                private Boolean alive = false;
                private Character gender;
                private Person relative;
                private List<Person> relativesList;
                private Person[] relativesArray;
            
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
            
                public int getNumberOfChildren() {
                    return numberOfChildren;
                }
            
                public void setNumberOfChildren(int numberOfChildren) {
                    this.numberOfChildren = numberOfChildren;
                }
            
                public Long getSsn() {
                    return ssn;
                }
            
                public void setSsn(Long ssn) {
                    this.ssn = ssn;
                }
            
                public Boolean getAlive() {
                    return alive;
                }
            
                public void setAlive(Boolean alive) {
                    this.alive = alive;
                }
            
                public Character getGender() {
                    return gender;
                }
            
                public void setGender(Character gender) {
                    this.gender = gender;
                }
            
                public Person getRelative() {
                    return relative;
                }
            
                public void setRelative(Person relative) {
                    this.relative = relative;
                }
            
                public List<Person> getRelativesList() {
                    return relativesList;
                }
            
                public void setRelativesList(List<Person> relativesList) {
                    this.relativesList = relativesList;
                }
            
                public Person[] getRelativesArray() {
                    return relativesArray;
                }
            
                public void setRelativesArray(Person[] relativesArray) {
                    this.relativesArray = relativesArray;
                }
            }
            
            public class PersonBuilder {

                private String firstName;
                private String lastName;
                private int numberOfChildren;
                private Long ssn;
                private Boolean alive = false;
                private Character gender;
                private Person relative;
                private List<Person> relativesList;
                private Person[] relativesArray;
            
            
                public PersonBuilder firstName(String firstName) {
                    this.firstName = firstName;
                    return this;
                }
            
                public PersonBuilder lastName(String lastName) {
                    this.lastName = lastName;
                    return this;
                }
            
                public PersonBuilder numberOfChildren(int numberOfChildren) {
                    this.numberOfChildren = numberOfChildren;
                    return this;
                }
            
                public PersonBuilder ssn(Long ssn) {
                    this.ssn = ssn;
                    return this;
                }
            
                public PersonBuilder alive(Boolean alive) {
                    this.alive = alive;
                    return this;
                }
            
                public PersonBuilder gender(Character gender) {
                    this.gender = gender;
                    return this;
                }
            
                public PersonBuilder relative(Person relative) {
                    this.relative = relative;
                    return this;
                }
            
                public Person build() {
                    Person person = new Person();
                    person.setFirstName(firstName);
                    person.setLastName(lastName);
                    person.setNumberOfChildren(numberOfChildren);
                    person.setSsn(ssn);
                    person.setAlive(alive);
                    person.setGender(gender);
                    person.setRelative(relative);
                    person.setRelativesList(relativesList);
                    person.setRelativesArray(relativesArray);
                    return person;
                }
            
                public PersonBuilder relativesList(List<Person> relativesList) {
                    this.relativesList = relativesList;
                    return this;
                }
            
                public PersonBuilder relativesArray(Person[] relativesArray) {
                    this.relativesArray = relativesArray;
                    return this;
                }
            }
            
            public class SomeBuilderWithNoBuilderSuffix {
            
                private String firstName;
                private String lastName;
            
                public PersonBuilder firstName(String firstName) {
                    this.firstName = firstName;
                    return this;
                }
            
                public PersonBuilder lastName(String lastName) {
                    this.lastName = lastName;
                    return this;
                }
            
                public Person build() {
                    Person person = new Person();
                    person.setFirstName(firstName);
                    person.setLastName(lastName);
                    return person;
                }
            }
            
            public class SomeClassWithNoBuildMethodBuilder {
            
                private String firstName;
                private String lastName;
            
                public PersonBuilder firstName(String firstName) {
                    this.firstName = firstName;
                    return this;
                }
            
                public PersonBuilder lastName(String lastName) {
                    this.lastName = lastName;
                    return this;
                }
            
                public Person buildThePojo() {
                    Person person = new Person();
                    person.setFirstName(firstName);
                    person.setLastName(lastName);
                    return person;
                }
            }

        """, """
            package sample;

            import java.util.List;
            import javax.annotation.Generated;
            import org.junit.Test;
            import static org.junit.Assert.assertEquals;
            
            @Generated("GeneralPatterns")
            public class FooPatternTest {
            
                @Test
                public void testBuildFirstName() {
                    // Arrange
                    String testData = "testData";
                    PersonBuilder builder = new PersonBuilder();
                    builder.firstName(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getFirstName());
                }
            
                @Test
                public void testBuildLastName() {
                    // Arrange
                    String testData = "testData";
                    PersonBuilder builder = new PersonBuilder();
                    builder.lastName(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getLastName());
                }
            
                @Test
                public void testBuildNumberOfChildren() {
                    // Arrange
                    int testData = 10;
                    PersonBuilder builder = new PersonBuilder();
                    builder.numberOfChildren(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getNumberOfChildren());
                }
            
                @Test
                public void testBuildSsn() {
                    // Arrange
                    Long testData = 10L;
                    PersonBuilder builder = new PersonBuilder();
                    builder.ssn(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getSsn());
                }
            
                @Test
                public void testBuildAlive() {
                    // Arrange
                    Boolean testData = true;
                    PersonBuilder builder = new PersonBuilder();
                    builder.alive(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getAlive());
                }
            
                @Test
                public void testBuildGender() {
                    // Arrange
                    Character testData = 'c';
                    PersonBuilder builder = new PersonBuilder();
                    builder.gender(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getGender());
                }
            
                @Test
                public void testBuildRelative() {
                    // Arrange
                    Person testData = new Person();
                    PersonBuilder builder = new PersonBuilder();
                    builder.relative(testData);
                    // Act
                    Person pojo = builder.build();
                    // Assert
                    assertEquals(testData, pojo.getRelative());
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new BuilderTestGenerator(reporter, visitReporter)
    }
}
