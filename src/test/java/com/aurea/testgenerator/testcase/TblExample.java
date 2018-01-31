package com.aurea.testgenerator.testcase;

public class TblExample {

    public static final int PECRTJL = 123;

    public void setPEcrtjl(String value) {
        if (BusinessData.containsData(value)
                && !TableUtility.isTbl(this, GestionTable.TP_TJL, value)) {
            throw new GnxJboException("P_TJLTYPIN", new Object[]{value});
        }

        setAttributeInternal(PECRTJL, value);
    }

    private void setAttributeInternal(int pecrtjl, String value) {

    }
}
