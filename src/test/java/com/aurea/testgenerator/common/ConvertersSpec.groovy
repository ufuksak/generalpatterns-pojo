package com.aurea.testgenerator.common

import com.aurea.testgenerator.Converters
import spock.lang.Specification


class ConvertersSpec extends Specification {

    def "should convert arbirary number to pascal case"() {
        expect:
        Converters.integerToPascalCaseWords(45) == "FortyFive"
    }
}
