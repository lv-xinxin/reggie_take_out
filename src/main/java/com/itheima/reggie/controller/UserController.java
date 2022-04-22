package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.config.Sms;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.entity.UserInfo;
import com.itheima.reggie.exceptions.CustomException;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @Autowired
    Sms sms;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            throw new CustomException("请正确输入手机号");
        }
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);

        sms.sendMsg(phone,codeNum);
        // 将其验证码储存到session中
        session.setAttribute(phone, codeNum);

        return R.success("手机验证码短信发送成功");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登陆成功");
    }

    @GetMapping("/get")
    public R<UserInfo> getUser(HttpSession session) {
        UserInfo userInfo = new UserInfo();
        Long userId = (Long) session.getAttribute("user");
        User user = userService.getById(userId);
        if (user.getName() == null) {
            userInfo.setUsername(user.getPhone());
        } else {
            userInfo.setUsername(user.getName());
        }
        if (user.getSex() == null) {
            userInfo.setSex("女");
        } else {
            userInfo.setSex("男");
        }
        return R.success(userInfo);
    }


























}
