package com.aurea.testgenerator.extensions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ASTExtensionDeclarations {

    @Autowired
    List<ASTExtension> astExtensions
}
