package com.example.gobang.controller;

import com.example.gobang.mapper.UserMapper;
import com.example.gobang.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.DuplicateFormatFlagsException;

/**
 * @author qiu
 * @version 1.8.0
 */
@ResponseBody
@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Object login(@RequestParam("username") String username, @RequestParam("password") String password,HttpServletRequest httpServletRequest){
        User user = userMapper.selectByName(username);
        if(user == null){
            System.out.println("登录失败");
            return new User();
        }
        //如果注册模块加密了,这里要用加密的判断.
        if(!password.equals(user.getPassword())){
            System.out.println("登录失败");
            return new User();
        }
        HttpSession  httpSession = httpServletRequest.getSession(true);
        httpSession.setAttribute("user",user);
        return user;
    }

    @PostMapping("/register")
    public Object register(String username,String password){
        //加密可以之后在注册这里使用.
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            return user;
        }catch (org.springframework.dao.DuplicateKeyException e){
            return new User();
        }
    }

    @GetMapping("/userInfo")
    public Object getUserInfo(HttpServletRequest httpServletRequest){
        try {
            HttpSession httpSession = httpServletRequest.getSession(false);
            User user = (User)httpSession.getAttribute("user");
            return user;
        }catch (NullPointerException e){
            return new User();
        }


    }
}
