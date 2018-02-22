package com.aurea.testgenerator.generation.patterns

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.MethodLevelTestGenerator
import com.aurea.testgenerator.generation.patterns.methods.AssignmentCheckAbstractFactoryMethodTestGenerator

class AssignmentCheckAbstractFactoryMethodTestGeneratorSpec extends MatcherPipelineTest {

    def "assigning default values should be asserted if fields are available"() {
        expect:
        onClassCodeExpect """
            class UserProfile {
    
                String username;
                int age;
                float degrees;
                boolean female;
                Object status;
                
                public static UserProfile emptyProfile(String username) {
                    UserProfile profile = new UserProfile();
                    profile.username = username;
                    profile.age = 42;
                    profile.degrees = 42.2F;
                    profile.female = false;
                    profile.status = new Object();
                    return profile;
                }   
            }
        """, """     
            package sample;
             
            import org.assertj.core.api.SoftAssertions;
            import org.assertj.core.data.Offset;
            import org.junit.Test;
             
            public class FooTest {
             
                @Test
                public void test_emptyProfile_AssignsValues() throws Exception {
                    String username = "ABC";
                    UserProfile profile = UserProfile.emptyProfile(username);
                    
                    SoftAssertions sa = new SoftAssertions();
                    sa.assertThat(profile.status).isEqualTo(new Object());
                    sa.assertThat(profile.female).isFalse();
                    sa.assertThat(profile.degrees).isCloseTo(42.2F, Offset.offset(0.001F));
                    sa.assertThat(profile.age).isEqualTo(42);
                    sa.assertThat(profile.username).isEqualTo(username);
                    sa.assertAll();
                }
            }
        """
    }

    def "assigning values via setters should be checked"() {
        expect:
        onClassCodeExpect """
            class UserProfile {
    
                private String username;
                private int age;
                private float degrees;
                private boolean female;
                private Object status;
                
                public static UserProfile emptyProfile(String username) {
                    UserProfile profile = new UserProfile();
                    profile.setUsername(username);
                    profile.setAge(42);
                    profile.setDegrees(42.2F);
                    profile.setFemale(false);
                    profile.setStatus(new Object());
                    return profile;
                }
                
                public void setUsername(String username) {
                    this.username = username;
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public void setDegrees(float degrees) {
                    this.degrees = degrees;
                }
                
                public void setFemale(boolean female) {
                    this.female = female;
                }
                
                public void setStatus(Object status) {
                    this.status = status;
                }
                
                public String getUsername() {
                    return username;
                }
                
                public int getAge() {
                    return age;
                }
                
                public float getDegrees() {
                    return degrees;
                }
                
                public boolean isFemale() {
                    return female;
                }
                
                public Object getStatus() {
                    return status;
                }
            }
        """, """     
            package sample;
             
            import org.assertj.core.api.SoftAssertions;
            import org.assertj.core.data.Offset;
            import org.junit.Test;
             
            public class FooTest {
             
                @Test
                public void test_emptyProfile_AssignsValues() throws Exception {
                    String username = "ABC";
                    UserProfile profile = UserProfile.emptyProfile(username);
                    
                    SoftAssertions sa = new SoftAssertions();
                    sa.assertThat(profile.getUsername()).isEqualTo(username);
                    sa.assertThat(profile.isFemale()).isFalse();
                    sa.assertThat(profile.getAge()).isEqualTo(42);
                    sa.assertThat(profile.getStatus()).isEqualTo(new Object());
                    sa.assertThat(profile.getDegrees()).isCloseTo(42.2F, Offset.offset(0.001F));
                    sa.assertAll();
                }
            }
        """
    }

    def "mixing setters and assignments is tested"() {
        expect:
        onClassCodeExpect """
            class UserProfile {
    
                String username;
                private int age;
                
                public static UserProfile emptyProfile(String username) {
                    UserProfile profile = new UserProfile();
                    profile.username = username;
                    profile.setAge(42);
                    return profile;
                }
                
                public void setAge(int age) {
                    this.age = age;
                }
                
                public int getAge() {
                    return age;
                }
            }
        """, """     
            package sample;
             
            import org.assertj.core.api.SoftAssertions;
            import static org.assertj.core.api.Assertions.assertThat;
            import org.junit.Test;
             
            public class FooTest {
             
                @Test
                public void test_emptyProfile_AssignsValues() throws Exception {
                    String username = "ABC";
                    UserProfile profile = UserProfile.emptyProfile(username);
                    
                    SoftAssertions sa = new SoftAssertions();
                    sa.assertThat(profile.username).isEqualTo(username);
                    sa.assertThat(profile.getAge()).isEqualTo(42);
                    sa.assertAll();
                }
            }
        """
    }

    @Override
    MethodLevelTestGenerator generator() {
        new AssignmentCheckAbstractFactoryMethodTestGenerator(solver, reporter, visitReporter, nomenclatureFactory, valueFactory)
    }
}
