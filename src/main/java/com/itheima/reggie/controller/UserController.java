package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itheima.reggie.common.R;
import com.itheima.reggie.config.RedisConfig;
import com.itheima.reggie.config.Sms;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.entity.UserInfo;
import com.itheima.reggie.exceptions.CustomException;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @Autowired
    Sms sms;

    @Autowired
    RedisTemplate redisTemplate;


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
//        session.setAttribute(phone, codeNum);

        //将生成验证码存到redis，并且有效时间3分钟
        redisTemplate.opsForValue().set(phone,codeNum,3, TimeUnit.MINUTES);

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
        log.info("map={}", map);

        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();
        // 从session中获取验证码
        // Object codeInSession = session.getAttribute(phone);

        // 从redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        // 验证码校验
        if (codeInSession == null) {
            return R.error("请先发送验证码");
        } else if (!codeInSession.equals(code)) {
            return R.error("验证码错误");
        } else {
            // 登录成功
            // 判断当前手机号是否为新用户，是新用户自动注册
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class);
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                // 新用户自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                user.setSex("1");
                userService.save(user);
            }
            session.setAttribute("user", user.getId());

            // 用户登录成功，删除验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
    }


    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登陆成功");
    }

    /**
     * 更新用户信息
     *
     * @param map     用户信息
     * @param session session
     * @return 用户信息
     */
    @PostMapping("/update")
    public R<User> update(@RequestBody Map map, HttpSession session) {
        Long userId = Long.parseLong(session.getAttribute("user").toString());

        log.info("用户id：{}", userId);

        String userSex = (String) map.get("userSex");
        log.info("要把性别修改为：{}", userSex);
        String userName = (String) map.get("userName");
        log.info("要把姓名修改为：{}", userName);

        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class);
        updateWrapper.eq(User::getId, userId);
        updateWrapper.set(userSex != null, User::getSex, userSex);
        updateWrapper.set(userName != null, User::getName, userName);

        userService.update(updateWrapper);
        User        user = userService.getById(userId);

        return R.success(user);
    }


























}
