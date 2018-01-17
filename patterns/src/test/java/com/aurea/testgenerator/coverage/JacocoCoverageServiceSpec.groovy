package com.aurea.testgenerator.coverage

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.source.PathToUnitMapper
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class JacocoCoverageServiceSpec extends Specification {

    private static final Path PARSER_EXAMPLES

    static {
        URL url = JacocoCoverageServiceSpec.class.getResource("")
        PARSER_EXAMPLES = Paths.get(url.toURI())
    }

    JacocoCoverageService service = new JacocoCoverageService(
            new JacocoCoverageRepository(PARSER_EXAMPLES.resolve('with-inner-classes.xml')))

    def "should correctly deduce inner type of the parent type of current type"() {
        when:
        Unit unit = getUnit('Library.java')
        MethodCoverage methodCoverage = getMethodCoverageForMethod(unit, 'repair')

        then:
        methodCoverage != MethodCoverage.EMPTY
    }

    def "should correctly deduce inner type"() {
        when:
        Unit unit = getUnit('Library.java')
        MethodCoverage methodCoverage = getMethodCoverageForMethod(unit, 'getBookFromTopShelf')

        then:
        methodCoverage != MethodCoverage.EMPTY
    }

    MethodCoverage getMethodCoverageForMethod(Unit unit, String methodName) {
        List<MethodDeclaration> methodDeclarations = ASTNodeUtils.findChildsOf(MethodDeclaration.class, unit.cu)
        MethodDeclaration md = methodDeclarations.find {
            it.nameAsString == methodName
        }
        service.getMethodCoverage(MethodCoverageQuery.of(unit, md))
    }

    static Unit getUnit(String fileName) {
        PathToUnitMapper pathToUnitMapper = new PathToUnitMapper(PARSER_EXAMPLES)
        pathToUnitMapper.apply(PARSER_EXAMPLES.resolve(Paths.get("org", "example", "innerness", fileName))).get()
    }
}
