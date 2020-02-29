package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.leyou.utils.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({FilterProperties.class,JwtProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        //获取网关上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        //获取请求路径
        String uri = request.getRequestURI();

        List<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (StringUtils.contains(uri, allowPath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //获取网关上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        //获取token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        //校验
        try {
            JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            //出现异常，返回403
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        }


        return null;
    }
}
