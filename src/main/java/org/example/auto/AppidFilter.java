package org.example.auto;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * a java servlet filter for get appid from http head and put it into the appIdHolder
 */
@Component
public class AppidFilter extends GenericFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String appid = request.getHeader("appid");
            AppIdHolder.appId.set(appid);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AppIdHolder.appId.set(null);
        }
    }
}
