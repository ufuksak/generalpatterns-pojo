package com.aurea.testgenerator.generation.patterns

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.patterns.methods.IsCallableAbstractFactoryMethodTestGenerator

class IsCallableAbstractFactoryMethodSpec extends MatcherPipelineTest {

    def "assigning integral literals should be asserted"() {
        expect:
        onClassCodeExpect """
            public class DbConnection{
               private static final int MAX_CONNS = 100;
               private static int totalConnections = 0;
            
               private static Set<DbConnection> availableConnections = new HashSet<DbConnection>();
            
               private DbConnection(){
                 // ...
                 totalConnections++;
               }
            
               public static DbConnection getDbConnection(){
            
                 if(totalConnections < MAX_CONNS){
                   return new DbConnection();
            
                 }else if(availableConnections.size() > 0){
                     DbConnection dbc = availableConnections.iterator().next();
                     availableConnections.remove(dbc);
                     return dbc;
            
                 }else {
                     throw new NoDbConnections();
                 }
               }
            
               public static void returnDbConnection(DbConnection dbc){
                 availableConnections.add(dbc);
                 //...
               }
            }
        """, """     
            package sample;
             
            import org.junit.Test;
             
            public class FooTest {
             
                @Test
                public void test_getDbConnection_IsCallable() throws Exception {
                    DbConnection.getDbConnection();
                }
            }
        """
    }

    @Override
    TestGenerator generator() {
        new IsCallableAbstractFactoryMethodTestGenerator(solver, reporter, visitReporter, nomenclatureFactory, valueFactory)
    }
}
