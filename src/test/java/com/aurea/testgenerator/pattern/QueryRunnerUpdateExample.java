package com.aurea.testgenerator.pattern;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryRunnerUpdateExample {

    public void foo(String idDocRegl) {
        Connection con = null;

        try {
            String sql = "delete from mvttiers where iddoccredit = ? ";

            QueryRunner qRunner = new QueryRunner();

            try {
                qRunner.update(con, sql, idDocRegl);
            } catch (SQLException e) {
            }
            con.commit();

        } catch (Exception e) {
        } finally {
        }
        System.out.println("deleteCreditToClient = " + idDocRegl);
    }
}
