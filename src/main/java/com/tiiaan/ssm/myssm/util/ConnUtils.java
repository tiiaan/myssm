package com.tiiaan.ssm.myssm.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.commons.dbutils.DbUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

public class ConnUtils {

    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();

    private static DataSource dataSource = null;
    static {
        try {
            InputStream is = ConnUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(is);
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    public static void closeResources(Connection conn, Statement s, ResultSet rs) {
        DbUtils.closeQuietly(conn);
        DbUtils.closeQuietly(s);
        DbUtils.closeQuietly(rs);
    }


    public static Connection getConn() {
        try {
            InputStream is = ConnUtils.class.getClassLoader().getResourceAsStream("jdbc2.properties");
            Properties info = new Properties();
            info.load(is);
            String driverName = info.getProperty("driverName");
            String url = info.getProperty("url");
            String user = info.getProperty("user");
            String password = info.getProperty("password");
            Class.forName(driverName);
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Connection getConnFromThreadLocal() throws SQLException {
        //1.从ThreadLocal中取出conn
        Connection conn = threadLocal.get();

        //2.如果conn==null就新造一个连接放到ThreadLocal中去
        if (conn == null) {
            conn = getConnection();
            threadLocal.set(conn);
        }
        return conn;
    }


    public static void closeConnFromThreadLocal() throws SQLException {
        Connection conn = threadLocal.get();
        if (conn != null) {
            //1.恢复自动提交, 使用数据库连接池的好习惯
            conn.setAutoCommit(true);

            //2.关闭conn
            conn.close();

            //3.清空ThreadLocal
            threadLocal.set(null);
        }
    }


    public static void printCurrConn() {
        System.out.println(threadLocal.get());
    }
}
