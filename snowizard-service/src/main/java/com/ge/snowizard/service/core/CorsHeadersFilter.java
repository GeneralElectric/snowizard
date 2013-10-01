package com.ge.snowizard.service.core;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public class CorsHeadersFilter implements Filter {

    private static final ImmutableSet<String> ALLOWED_HEADERS = ImmutableSet
            .of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION,
                    HttpHeaders.ACCEPT, "Origin", "X-Requested-With");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.addHeader("X-Frame-Options", "deny");
            httpServletResponse.addHeader("X-XSS-Protection", "1; mode=block");
            httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
            if ("OPTIONS".equals(httpServletRequest.getMethod())) {
                httpServletResponse.addHeader("Access-Control-Allow-Headers",
                        Joiner.on(", ").join(ALLOWED_HEADERS));
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // no configuration required
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
