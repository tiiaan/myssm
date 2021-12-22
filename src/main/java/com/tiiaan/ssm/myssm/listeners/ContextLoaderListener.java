package com.tiiaan.ssm.myssm.listeners;

import com.tiiaan.ssm.myssm.ioc.BeanFactory;
import com.tiiaan.ssm.myssm.ioc.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */


@WebListener()
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //1.创建IOC容器
        BeanFactory beanFactory = new ClassPathXmlApplicationContext();

        //2.保存到application作用域
        servletContextEvent.getServletContext().setAttribute("beanFactory", beanFactory);

        System.out.println("IOC container init success..........");
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
