package com.tiiaan.ssm.myssm.basedao;

import com.tiiaan.ssm.myssm.exceptions.BaseDAOException;
import com.tiiaan.ssm.myssm.util.ConnUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

public class BaseDAO<T> {
    Class<T> clazz = null;
    {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        ParameterizedType type = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = type.getActualTypeArguments();
        clazz = (Class<T>) actualTypeArguments[0];
    }


    public int executeUpdate(String sql, Object ...args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = ConnUtils.getConnFromThreadLocal();
            boolean isInsert = false;
            isInsert = sql.trim().toUpperCase().startsWith("INSERT");
            if (isInsert) {
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                ps = conn.prepareStatement(sql);
            }
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            int res = ps.executeUpdate();
            if (isInsert) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return ((Long)rs.getLong(1)).intValue();
                }
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BaseDAOException("ERROR in BaseDAO.");
        } finally {
            ConnUtils.closeResources(null, ps, rs);
        }
    }


    public static void updateAlter(String sql) {
        try {
            Connection conn = ConnUtils.getConnFromThreadLocal();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BaseDAOException("ERROR in BaseDAO.");
        }
    }




    public T getInstance(String sql, Object ...args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = ConnUtils.getConnFromThreadLocal();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            if (rs.next()) {
                T t = clazz.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    Object columnValue = rs.getObject(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);
                    Field field = clazz.getDeclaredField(columnLabel);

                    //获取当前字段的类型名称
                    String typeName = field.getType().getName();
                    //System.out.println(typeName);

                    //判断如果是自定义类型，则需要调用这个自定义类的带一个参数的构造方法，创建出这个自定义的实例对象，然后将实例对象赋值给这个属性
                    if(isMyType(typeName)){
                        Class typeNameClass = Class.forName(typeName);
                        //Constructor constructor = typeNameClass.getDeclaredConstructor(Integer.class);
                        Constructor constructor = typeNameClass.getDeclaredConstructor(columnValue.getClass());
                        columnValue = constructor.newInstance(columnValue);
                    }

                    field.setAccessible(true);
                    field.set(t, columnValue);
                }
                return t;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseDAOException("ERROR in BaseDAO.");
        } finally {
            ConnUtils.closeResources(null, ps, rs);
        }
    }



    public List<T> getForList(String sql, Object ...args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = ConnUtils.getConnFromThreadLocal();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            ArrayList<T> list = new ArrayList<>();
            while (rs.next()) {
                T t = clazz.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    Object columnValue = rs.getObject(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);
                    Field field = clazz.getDeclaredField(columnLabel);

                    //获取当前字段的类型名称
                    String typeName = field.getType().getName();

                    //判断如果是自定义类型，则需要调用这个自定义类的带一个参数的构造方法，创建出这个自定义的实例对象，然后将实例对象赋值给这个属性
                    if(isMyType(typeName)){
                        Class typeNameClass = Class.forName(typeName);
                        //Constructor constructor = typeNameClass.getDeclaredConstructor(Integer.class);
                        Constructor constructor = typeNameClass.getDeclaredConstructor(columnValue.getClass());
                        columnValue = constructor.newInstance(columnValue);
                    }

                    field.setAccessible(true);
                    field.set(t, columnValue);
                }
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseDAOException("ERROR in BaseDAO.");
        } finally {
            ConnUtils.closeResources(null, ps, rs);
        }
    }



    public static Object getValue(String sql, Object ...args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = ConnUtils.getConnFromThreadLocal();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BaseDAOException("ERROR in BaseDAO.");
        } finally {
            ConnUtils.closeResources(null, ps, rs);
        }
    }



    private static boolean isMyType(String typeName) {
        return !isNotMyType(typeName);
    }
    private static boolean isNotMyType(String typeName) {
        return "java.lang.Byte".equals(typeName) ||
                "java.lang.Short".equals(typeName) ||
                "java.lang.Integer".equals(typeName) ||
                "java.lang.Long".equals(typeName) ||
                "java.lang.Float".equals(typeName) ||
                "java.lang.Double".equals(typeName) ||
                "java.lang.Character".equals(typeName) ||
                "java.lang.Boolean".equals(typeName) ||
                "java.lang.String".equals(typeName) ||
                "java.util.Date".equals(typeName) ||
                "java.sql.Date".equals(typeName) ||
                "java.time.Instant".equals(typeName);
    }
}
