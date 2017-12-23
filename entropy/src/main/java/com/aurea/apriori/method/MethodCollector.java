package com.aurea.apriori.method;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Mihai Raulea on 04/12/2017.
 */
public class MethodCollector {

    public Set<String> allReferencedTypes = new HashSet<>();
    public List<MethodInfoMLInput> inputMethods = new LinkedList<>();
    MethodInfoMLInput methodInfoMLInput = new MethodInfoMLInput();

    public void ingestLine(String line) {
        if (!line.contains("- !MethodMetaInformation")) {
            methodInfoMLInput.addNextString(line);
        } else {
            inputMethods.add(methodInfoMLInput);
            allReferencedTypes.addAll(methodInfoMLInput.referencedTypes);
            methodInfoMLInput = new MethodInfoMLInput();
        }
    }

}
