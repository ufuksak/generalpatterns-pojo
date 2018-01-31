package com.aurea.testgenerator.generation

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class Generators {

    @Autowired
    Generators(Map<String, TestGenerator> generators) {
        
    }

}
