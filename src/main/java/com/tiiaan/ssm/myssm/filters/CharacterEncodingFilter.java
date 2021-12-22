package com.tiiaan.ssm.myssm.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

@WebFilter(urlPatterns = {"*.do"}, initParams = {@WebInitParam(name="encoding", value="UTF-8")})
public class CharacterEncodingFilter implements Filter {

    private String encoding = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        encoding = filterConfig.getInitParameter("encoding");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //设置编码
        ((HttpServletRequest) servletRequest).setCharacterEncoding(encoding);

        //放行
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
