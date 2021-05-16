package cn.com.kunghsu.desktop.controller;

import cn.com.kunghsu.desktop.model.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 应用界面访问入口
 * Created by xuyaokun On 2021/5/16 16:46
 * @desc:
 */
@RequestMapping("/")
@Controller
public class MainController {

    private Result keepaliveResult = new Result();

    @RequestMapping("/")
    public String index(HttpSession session, HttpServletRequest request) {
        session.setMaxInactiveInterval(60);
        session.setAttribute("remote-addr", request.getRemoteAddr());
        return "index";
    }

    @RequestMapping("/keepalive")
    @ResponseBody
    public Result keepalive() {
        // 什么都不做，只是为了保持会话
        return keepaliveResult;
    }

    private void sleep(int ms) {
        try
        {
            Thread.sleep(ms);
        }
        catch(Exception e) { }
    }
}
