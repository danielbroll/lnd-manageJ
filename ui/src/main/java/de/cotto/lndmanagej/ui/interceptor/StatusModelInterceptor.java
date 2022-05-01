package de.cotto.lndmanagej.ui.interceptor;

import de.cotto.lndmanagej.ui.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StatusModelInterceptor implements HandlerInterceptor {

    @Autowired
    StatusService statusService;

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) {
        if (modelAndView != null) {
            modelAndView.getModel().put("status", statusService.getStatus());
        }
    }
}