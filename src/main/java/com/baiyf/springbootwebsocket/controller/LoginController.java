package com.baiyf.springbootwebsocket.controller;

import com.baiyf.springbootwebsocket.bean.ResponseBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String defaultLogin (Model model) {
        // 已经登录过了
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return "redirect:/chat";
        }

        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean login(@RequestParam("name") String username, @RequestParam("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        ResponseBean response = new ResponseBean();
        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            response.setCode(-1);
            response.setData("");
            response.setMsg("未知账户");

            return response;
        } catch (IncorrectCredentialsException ice) {
            response.setCode(-2);
            response.setData("");
            response.setMsg("密码不正确");

            return response;
        } catch (LockedAccountException lae) {
            response.setCode(-3);
            response.setData("");
            response.setMsg("账户已锁定");

            return response;
        } catch (ExcessiveAttemptsException eae) {
            response.setCode(-4);
            response.setData("");
            response.setMsg("用户名或密码错误次数过多");

            return response;
        } catch (AuthenticationException ae) {
            response.setCode(-5);
            response.setData("");
            response.setMsg("用户名或密码不正确！");

            return response;
        }

        if (subject.isAuthenticated()) {
            response.setCode(0);
            response.setData("/chat");
            response.setMsg("登录成功");

            return response;
        } else {
            token.clear();
            response.setCode(-6);
            response.setData("");
            response.setMsg("登录失败");

            return response;
        }
    }
}
