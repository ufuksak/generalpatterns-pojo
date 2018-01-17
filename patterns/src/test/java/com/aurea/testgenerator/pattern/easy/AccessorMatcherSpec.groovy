package com.aurea.testgenerator.pattern.easy

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.coverage.JacocoCoverageService
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.xml.XPathEvaluatorImpl
import com.github.generator.xml.Converters
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import spock.lang.Specification

import javax.xml.xpath.XPathFactory

import static com.aurea.testgenerator.UnitHelper.getUnitForCode

class AccessorMatcherSpec extends Specification {

    JacocoCoverageService coverageService = Mock(JacocoCoverageService)
    JavaParserFacade javaParserFacade = Mock(JavaParserFacade)
    AccessorMatcher matcher = new AccessorMatcher(coverageService, javaParserFacade)
    ResolvedFieldDeclaration fieldDeclaration = Mock()
    ResolvedValueDeclaration valueDeclaration = Mock()
    ResolvedParameterDeclaration parameterDeclaration = Mock()

    def setup() {
        coverageService.getMethodCoverage(_) >> new MethodCoverage("has-coverage", 1, 1, 1, 1)

        SymbolReference<ResolvedValueDeclaration> reference = Mock(SymbolReference)
        reference.correspondingDeclaration >> this.valueDeclaration
        reference.solved >> true
        valueDeclaration.isField() >> true
        valueDeclaration.asField() >> fieldDeclaration
        valueDeclaration.isParameter() >> true
        valueDeclaration.asParameter() >> parameterDeclaration

        javaParserFacade.solve(_) >> reference

        matcher.xmlConverter = Converters.newConverter()
        matcher.evaluator = new XPathEvaluatorImpl(XPathFactory.newInstance().newXPath())
    }


    def "should find getter returning constant literal value"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public Object getFoo() {
  return $literal;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == expectation
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL

        where:
        literal   | expectation
        3         | "3"
        "\"ABC\"" | "\"ABC\""
        3.0       | "3.0"
        "3L"      | "3L"
        "'c'"     | "'c'"
        false     | "false"
        true      | "true"
    }

    def "should find simple field accessors even if without a setter if field is public"() {
        given:
        fieldDeclaration.name >> "bar"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public int bar;
 public int getFoo() {
  return bar;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).canBeAccessedDirectly
        (matches.first() as AccessorMatch).fieldName.get().nameAsString == "bar"
    }

    def "should find simple field accessors even if without a setter if field is package private"() {
        given:
        fieldDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 int foo;
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).canBeAccessedDirectly
    }

    def "should find simple field accessors and find setter"() {
        given:
        fieldDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private int foo;
 
 public void setFoo(int foo) {
  this.foo = foo;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).modifier.get().nameAsString == 'setFoo'
    }

    def "should find getter returning class constant"() {
        given:
        fieldDeclaration.name >> "FOO"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private static final int FOO = $classConstant;
 
 public int getFoo() {
  return FOO;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == expectation
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL

        where:
        classConstant                   | expectation
        123                             | "123"
        "ABC"                           | "ABC"
        "System.getProperty(\"javac\")" | "System.getProperty(\"javac\")"
    }

    def "should find getter returning class constant when they are declared in the same line"() {
        given:
        fieldDeclaration.name >> "BAR"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private static final int FOO = 123, BAR = 456;
 
 public int getBar() {
  return BAR;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == '456'
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
    }

    def "should find initialization for class constants in static blocks"() {
        given:
        fieldDeclaration.name >> "FOO"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private static final int FOO;
 
 static {
  FOO = 123;
 }
 
 public int getFoo() {
  return FOO;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == '123'
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
    }

    def "should find getters returning final field initialized on declaration"() {
        given:
        fieldDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo = $constant;
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == expectation
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.OBJECT_CONSTANT

        where:
        constant                        | expectation
        123                             | "123"
        "ABC"                           | "ABC"
        "System.getProperty(\"javac\")" | "System.getProperty(\"javac\")"
    }

    def "should find getter for final fields which are initialized in public constructors"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 public Foo(int foo) {
  this.foo = foo;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructorInitialization.isPresent()
        (matches.first() as AccessorMatch).constructorInitialization.get().constructorParameterInitializer.nameAsString == 'foo'
    }

    def "should find getter for final fields which are initialized in public constructors even when name of parameter is different"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "bar"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 public Foo(int bar) {
  this.foo = bar;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructorInitialization.isPresent()
        (matches.first() as AccessorMatch).constructorInitialization.get().constructorParameterInitializer.nameAsString == 'bar'
    }

    def "should find getter for final fields which are initialized in public constructors even when name of parameter is different and no 'this'"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "bar"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 public Foo(int bar) {
  foo = bar;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructorInitialization.isPresent()
        (matches.first() as AccessorMatch).constructorInitialization.get().constructorParameterInitializer.nameAsString == 'bar'
    }

    def "should find getter for final fields which are initialized in public constructors if initialized with a constant"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 public Foo() {
  this.foo = 123;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructorInitialization.isPresent()
        (matches.first() as AccessorMatch).constructorInitialization.get().constantInitialValue == '123'
    }

    def "should not find getter for final fields which are initialized in private constructor"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 private Foo(int foo) {
  this.foo = foo;
 }
 
 protected Foo(int foo) {
  this.foo = foo;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.empty
    }

    def "should find getter for multiple constructors"() {
        given:
        fieldDeclaration.name >> "foo"
        parameterDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private final int foo;
 
 public Foo(int foo) {
  this(foo, "");
 }
 
 public Foo(int foo, String bar) {
  this.foo = foo;
 }
 
 public int getFoo() {
  return foo;
 }
}
""")

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructorInitialization.isPresent()
        (matches.first() as AccessorMatch).constructorInitialization.get().constructorParameterInitializer.nameAsString == 'foo'
    }

    def "should find static getters for class constants"() {
        given:
        fieldDeclaration.name >> "FOO"
        parameterDeclaration.name >> "foo"
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 private static final int FOO = 123;
 
 public static int getFoo() {
  return FOO;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.isPresent()
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
        (matches.first() as AccessorMatch).constantReturnValue.get().value == '123'
    }

    def "should report available constructor"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public Foo() {
 }
 public Object getFoo() {
  return 123;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructor.isPresent()
        (matches.first() as AccessorMatch).constructor.get().nameAsString == 'Foo'
        (matches.first() as AccessorMatch).constructor.get().parameters.isEmpty()
    }

    def "should report default constructor availability"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public Object getFoo() {
  return 123;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constructor.isPresent()
        (matches.first() as AccessorMatch).constructor.get() == AccessorMatch.DEFAULT_CONSTRUCTOR
    }

    def "should ignore singletons"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public static Foo getInstance() {
  return instance;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.isEmpty()
    }

    def "should ignore delegation getter"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public int getFoo() {
  return _getFoo("foo");
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.isEmpty()
    }

    def "should be able to concatenate long strings"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 public String getFoo() {
  return "foo" + "bar" + "milk" + "peanuts" + 5;
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == "\"foobarmilkpeanuts5\""
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
    }

    def "should ignore comments in accessors"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
    public long getId() {
        // L'id de LIMPORTARTICLE n'est pas un long. On ne cherche pas à
        // retourner une valeur valide.
        return 0;
    }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().value == "0"
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
    }

    def "should set type of constant to null when found null literal"() {
        given:
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
    public Long getId() {
        return null;
    }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.NULL
    }

    def "should not be able to concatenate strings and variables"() {
        given:
        fieldDeclaration.name >> 'someVariable'
        Optional<Unit> unit = getUnitForCode(
                """
class Foo {
 int someVariable = 45;
  
 public String getFoo() {
  return "foo" + someVariable + "bar";
 }
}
""")
        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.isEmpty()
    }

    def "should handle getters for constants in enum"() {
        given:
        javaParserFacade.solve(_) >> SymbolReference.unsolved(ResolvedValueDeclaration)
        fieldDeclaration.name >> 'VAL'
        Optional<Unit> unit = getUnitForCode(
                """
enum Foo {
    FIRST {
     private static final int VAL = 123;
     
     public int getVal() {
        return VAL;
     }
    };
    
    abstract int getVal();
}
"""
        )

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.isPresent()
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
        (matches.first() as AccessorMatch).constantReturnValue.get().value == '123'
    }

    def "should handle getters for shared constants within enum"() {
        given:
        fieldDeclaration.name >> 'VAL'
        Optional<Unit> unit = getUnitForCode(
                """
enum Foo {
    FIRST, 
    SECOND;
    
    public int getVal() {
       return 123;
    }
}
"""
        )

        when:
        Collection<PatternMatch> matches = matcher.getMatches(unit.get())

        then:
        matches.size() == 1
        (matches.first() as AccessorMatch).constantReturnValue.isPresent()
        (matches.first() as AccessorMatch).constantReturnValue.get().type == AccessorConstantType.LITERAL
        (matches.first() as AccessorMatch).constantReturnValue.get().value == '123'
        (matches.first() as AccessorMatch).inEnum
        (matches.first() as AccessorMatch).enumDeclaration.get().nameAsString == 'FIRST'
    }
}
