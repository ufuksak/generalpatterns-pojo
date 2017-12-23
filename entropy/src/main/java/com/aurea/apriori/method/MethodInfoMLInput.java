package com.aurea.apriori.method;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mihai Raulea on 03/12/2017.
 */
public class MethodInfoMLInput {

    public List<String> referencedTypes = new LinkedList<>();
    boolean collectingReferencedTypes = false;

    public void addNextString(String stringToParse) {
        //System.out.println(stringToParse); //+ " sent to parsing");
        if(stringToParse.contains(":")) {
            collectingReferencedTypes = stringToParse.contains("referencedTypes:");
        }
        if(collectingReferencedTypes) {
            if(!stringToParse.contains("referencedTypes")) {
                //System.out.println(stringToParse+ "@@@");
                referencedTypes.add(stringToParse.replace("- ","").replace("   ",""));
            }
        }
    }

}
