package com.aurea.testgenerator.testcase;

public class InOutExample {

    private static final int ID = 123;

    public AllOut foo(AllIn in) {
        AllOut out = (AllOut) createBeanOut(InOutExample.ID);

        try {
            fillBeanIn(in);

            fillBeanOut(out);

            out.setId("456");

            foo();
        } catch (GnxJboException e) {
        } finally {
        }
        return out;
    }

    private void fillBeanOut(AllOut out) {
        out.setId("777");
    }

    private void fillBeanIn(AllIn in) {
        in.setMode("123");
    }

    private void foo() {
        throw new RuntimeException("Never ever");
    }

    private AllOut createBeanOut(int id) {
        return new AllOut();
    }

}
