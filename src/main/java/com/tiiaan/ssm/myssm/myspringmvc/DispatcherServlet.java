package com.tiiaan.ssm.myssm.myspringmvc;

import com.tiiaan.ssm.myssm.ioc.BeanFactory;
import com.tiiaan.ssm.myssm.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;



/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description 中央状态控制器
 */

@WebServlet("*.do")
public class DispatcherServlet extends ViewBaseServlet {

    BeanFactory beanFactory = null;

    @Override
    public void init() throws ServletException {
        //1.调用ViewBaseServlet中的init(), 从web.xml中读取视图前后缀
        super.init(); //千万不要忘记了!!!

        //2.初始化IOC容器 (抛弃)
        //beanFactory = new ClassPathXmlApplicationContext("applicationContext.xml");

        //2.从ServletContext中获取IOC容器
        Object beanFactoryObj = getServletContext().getAttribute("beanFactory");
        if (beanFactoryObj != null) {
            beanFactory = (BeanFactory) beanFactoryObj;
        } else {
            throw new RuntimeException("ioc init failed.");
        }

        System.out.println("DispatcherServlet init..........");
    }



    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //第一阶段, 定位到Controller
        //1.1 设置编码
        //req.setCharacterEncoding("UTF-8");

        //1.2 获取servletPath=/fruits.do
        String servletPath = req.getServletPath();

        //1.3 截取字符串servletPath=fruits
        servletPath = servletPath.substring(1); //fruits.do
        int lastIndexOf = servletPath.lastIndexOf(".do");
        servletPath = servletPath.substring(0, lastIndexOf); //fruits

        //1.4 获取 servletPath=fruits 对应的 FruitsController 类的对象
        Object controllerBeanObj = beanFactory.getBean(servletPath);



        //第二阶段, 定位到对应Controller中的指定方法
        //2.1 从请求参数中获取 operate
        req.setCharacterEncoding("UTF-8");
        String operate = req.getParameter("operate");
        if (StringUtils.isNullOrEmpty(operate)) {
            operate = "index";
        }

        try {
            //2.2 反射获取 Controller 类中的方法
            Method[] methods = controllerBeanObj.getClass().getDeclaredMethods();

            //第三阶段, 调用指定方法, 前提是: 方法名=operate值
            for (Method method : methods) {
                if (operate.equals(method.getName())) {

                    //3.1 我们不知道方法需要哪些形参, 所以先获取一下形参列表
                    Parameter[] params = method.getParameters();

                    //3.2 按照形参列表获取请求参数
                    Object[] paramValues = new Object[params.length];
                    for (int i = 0; i < params.length; i++) {
                        Parameter param = params[i]; //形参
                        String paramName = param.getName(); //形参名
                        String typeName = param.getType().getName(); //形参类型名
                        Object paramObj = null; //实参

                        if ("req".equals(paramName) || "request".equals(paramName)) {
                            paramObj = req;
                        } else if ("resp".equals(paramName) || "response".equals(paramName)) {
                            paramObj = resp;
                        } else if ("session".equals(paramName)) {
                            paramObj = req.getSession();
                        } else {
                            String paramValue = req.getParameter(paramName); //请求参数, String
                            paramObj = paramValue;
                            if (paramObj != null) {
                                //获取到的paramValue是字符串，如果形参是Integer，需要转换一下
                                if ("java.lang.Integer".equals(typeName)) {
                                    paramObj = Integer.parseInt(paramValue);
                                } else if ("java.lang.Double".equals(typeName)) {
                                    paramObj = Double.parseDouble(paramValue);
                                }
                            }
                        }
                        paramValues[i] = paramObj;
                    }

                    //3.3 执行方法, 返回资源跳转地址字符串
                    method.setAccessible(true);
                    Object invoke = method.invoke(controllerBeanObj, paramValues);

                    //3.4 视图处理
                    String returnPath = (String) invoke;
                    if (returnPath != null) {
                        if (returnPath.startsWith("redirect:")) {
                            String redirectPath = returnPath.substring("redirect:".length());
                            resp.sendRedirect(redirectPath); //重定向
                        } else if (returnPath.startsWith("json:")) {
                            String jsonStr = returnPath.substring("json:".length());
                            PrintWriter writer = resp.getWriter();
                            writer.print(jsonStr);
                            writer.flush();
                        } else {
                            super.processTemplate(returnPath, req, resp); //Thymeleaf转发
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
