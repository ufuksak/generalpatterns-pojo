package com.aurea.testgenerator.pattern;

import java.util.List;

public class BusinessValidationExample extends BaseBusinessValidationExample {

    private static final int UNMODIFIED = 123;
    private static final int NEW = 456;
    private Object codsoc1;
    private List<String> list;

    @Override
    public void businessValidation() {
        if (getRowStateGnx() == UNMODIFIED) {
            return;
        }

        if (getRowStateGnx() == NEW) {
            if (getCodsoc1() == null || GnxNumber.ZERO.equals(getCodsoc1())) {
                setCodsoc1(getCodsocPhy("P_ABN", null));
            }
        }

        super.businessValidation();
    }

    private Object getCodsocPhy(String p_abn, Object o) {
        return null;
    }

    private int getRowStateGnx() {
        return UNMODIFIED;
    }

    public Object getCodsoc1() {
        return null;
    }

    public Object setCodsoc1(Object obj) {
        return null;
    }

    private static class GnxNumber {
        private static final Integer ZERO = 0;
    }
}
