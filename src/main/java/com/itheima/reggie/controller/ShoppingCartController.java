package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.BaseContext;

import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.ShoppingDto;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.exceptions.CustomException;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        // 首先查询在其用户的购物表中是否存在 如果存在则在此基础上添加数量即可 如不存在则添加
        // 获取用户的id

        QueryWrapper<ShoppingCart> wrapper = new QueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new CustomException("请用户先进行登陆");
        }
        shoppingCart.setUserId(userId);

        wrapper.eq("user_id", userId);

        if (shoppingCart.getDishId() != null) { // 表示本次为菜品
            wrapper.eq("dish_id", shoppingCart.getDishId());
        } else {
            wrapper.eq("setmeal_id", shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingOne = shoppingCartService.getOne(wrapper);

        if (shoppingOne == null) {
            // 表示用户第一次添加购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingOne = shoppingCart;
        } else {
            // 表示用户在其购物车中已经添加
            shoppingOne.setNumber(shoppingOne.getNumber() + 1);
            shoppingCartService.updateById(shoppingOne);
        }

        return R.success(shoppingOne);
    }


    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        // 根据用户的id查询购物车
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new CustomException("请先登陆后在查看");
        }
        QueryWrapper<ShoppingCart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");

        List<ShoppingCart> cartList = shoppingCartService.list(wrapper);
        return R.success(cartList);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new CustomException("请先登陆在进行清空");
        }

        QueryWrapper<ShoppingCart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        shoppingCartService.remove(wrapper);
        return R.success("购物车清空成功");
    }

    @PostMapping("/sub")
    public void sub(@RequestBody ShoppingDto shoppingDto) {
        QueryWrapper<ShoppingCart> wrapper = new QueryWrapper<>();
        if (StringUtils.isEmpty(shoppingDto.getDishId())) {
            wrapper.eq("setmeal_id", shoppingDto.getSetmealId());
        } else {
            wrapper.eq("dish_id", shoppingDto.getDishId());
        }


        ShoppingCart shoppingCart = shoppingCartService.getOne(wrapper);

        if (shoppingCart.getNumber() == 0) {
            return;
        }

        if (shoppingCart.getNumber() == 1) {
            // 进行删除
            shoppingCartService.removeById(shoppingCart.getId());
        } else {
            shoppingCart.setNumber(shoppingCart.getNumber() - 1);
            shoppingCartService.updateById(shoppingCart);
        }
    }

}