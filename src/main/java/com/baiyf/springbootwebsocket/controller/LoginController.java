package com.baiyf.springbootwebsocket.controller;

import com.baiyf.springbootwebsocket.bean.ResponseBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @GetMapping(value = "/login")
    public String defaultLogin (HttpServletRequest request, Model model) {
        // 已经登录过了
        String basePath = (String) request.getAttribute("basePath");
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return "redirect:"+basePath+"/chat";
        }

        return "login";
    }

    @GetMapping(value = "/getBasePath")
    @ResponseBody
    public ResponseBean getBasePath(HttpServletRequest request) {
        String basePath = (String) request.getAttribute("basePath");
        return new ResponseBean(0, basePath, "success");
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public ResponseBean login(HttpServletRequest request, @RequestParam("name") String username, @RequestParam("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return new ResponseBean(-1, "", "未知账户");
        } catch (IncorrectCredentialsException ice) {
            return new ResponseBean(-2, "", "密码不正确");
        } catch (LockedAccountException lae) {
            return new ResponseBean(-3, "", "账户已锁定");
        } catch (ExcessiveAttemptsException eae) {
            return new ResponseBean(-4, "", "用户名或密码错误次数过多");
        } catch (AuthenticationException ae) {
            return new ResponseBean(-5, "", "用户名或密码不正确！");
        }

        if (subject.isAuthenticated()) {
            String basePath = (String) request.getAttribute("basePath");
            return new ResponseBean(0, basePath+"/chat", "登录成功");
        } else {
            token.clear();
            return new ResponseBean(-6, "", "登录失败");
        }
    }
}
