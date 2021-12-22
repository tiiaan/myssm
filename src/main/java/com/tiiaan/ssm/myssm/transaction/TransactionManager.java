package com.tiiaan.ssm.myssm.transaction;

import com.tiiaan.ssm.myssm.util.ConnUtils;

import java.sql.SQLException;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

public class TransactionManager {

    public static void begin() throws SQLException {
        ConnUtils.getConnFromThreadLocal().setAutoCommit(false);
    }


    public static void commit() throws SQLException {
        //1.提交
        ConnUtils.getConnFromThreadLocal().commit();

        //2.关闭连接, 清空ThreadLocal
        ConnUtils.closeConnFromThreadLocal();
    }


    public static void rollback() throws SQLException {
        //1.回滚
        ConnUtils.getConnFromThreadLocal().rollback();

        //2.关闭连接, 清空ThreadLocal
        ConnUtils.closeConnFromThreadLocal();
    }

}
