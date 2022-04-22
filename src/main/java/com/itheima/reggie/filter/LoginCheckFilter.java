package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import sun.java2d.pipe.AAShapePipe;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登入
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路劲匹配器
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取url
        String requestURI = request.getRequestURI();

        log.info("拦截到请求:{}",requestURI);

        //不需要请求的路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"


        };
        //判断请求是否要处理
        boolean check = check(urls, requestURI);
        //不处理，放行
        if (check) {
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        //判断是否登录,已登录，放行
       if(request.getSession().getAttribute("employee")!=null){
           log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("employee"));


           Long empId = (Long) request.getSession().getAttribute("employee");
           BaseContext.setCurrentId(empId);

           filterChain.doFilter(request, response);
           return;
       }

        //判断是否登录,已登录，放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("user"));


            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }


       log.info("用户未登录");
        //如果没有登录
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路劲匹配
     */
    public boolean check (String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }


}
