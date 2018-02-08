package com.aurea.testgenerator.ast

import com.github.generator.xml.Converters
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ConstructorDeclaration
import spock.lang.Specification

class FooSpec extends Specification {

    def "sss"() {
        expect:
        CompilationUnit cu = JavaParser.parse("""
            /*
 * Copyright:  2008 Acorn Systems, Inc.
 *
 * No part of this code may be reproduced, stored in a retrieval system, or transmitted,
 in any form, or by any means, electronic, mechanical, photocopied, recorded, or otherwise,
 without the prior written consent of Acorn Systems, Inc.  Acorn Enterprise Profit System (EPS),
 Acorn Reports, Profit Analyzer, Profit Optimizer, Acorn, Acorn Systems, and the acorn logo are
 trademarks of Acorn Systems, Inc. All other trademarks are owned by their manufacturers.
 All Rights Reserved.
 */
package com.acornsys.mmjavaapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import com.acornsys.mmjavaapi.objectmanager.ObjectMgr;

public class AcornDaemon {
    protected class AcornDaemonRunTask {

        private long frequencyMills;

        private long nextRunMills;

        private IAcornDaemonRunnable obj;

        public AcornDaemonRunTask(IAcornDaemonRunnable runMe, long frequencyMills) {
            this.frequencyMills = frequencyMills;
            this.nextRunMills = System.currentTimeMillis() + frequencyMills;
            this.obj = runMe;
        }

        public long getNextRunMills() {
            return nextRunMills;
        }

        public void setNextRunMills() {
            this.nextRunMills = System.currentTimeMillis() + frequencyMills;
        }

        public IAcornDaemonRunnable getRunObj() {
            return obj;
        }
    }
}

        """)

        println cu.findAll(ConstructorDeclaration).parentNode
//        println Converters.newConverter().toXmlString(cu)
        false && 'what does this test do?'
    }
}
