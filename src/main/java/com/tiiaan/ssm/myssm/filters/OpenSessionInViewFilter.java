package com.tiiaan.ssm.myssm.filters;

import com.tiiaan.ssm.myssm.transaction.TransactionManager;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */


@WebFilter("*.do")
public class OpenSessionInViewFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            //1.开启事务
            TransactionManager.begin();
            System.out.println("BEGIN transaction..........");

            //2.放行, 处理请求
            long start = System.currentTimeMillis();
            filterChain.doFilter(servletRequest, servletResponse);
            long end = System.currentTimeMillis();

            //3.提交并关闭连接
            TransactionManager.commit();
            System.out.println("COMMIT transaction..........elapsed time " + (end - start) + " ms.\n");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                //回滚并关闭连接
                TransactionManager.rollback();
                System.out.println("ROLLBACK transaction..........\n");
            } catch (SQLException ex) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void destroy() {

    }
}
