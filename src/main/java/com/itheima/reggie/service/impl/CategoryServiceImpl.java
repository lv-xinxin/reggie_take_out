package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.exceptions.CustomException;
import com.itheima.reggie.exceptions.CustomExceptionEnum;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    /**
     * 根据id删除分类，删除之间检查分类是否是否已经关联了菜品或套餐,关联则删除失败
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 1. 查询分类是否关联菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId, id);
        int count = dishService.count(lqw);
        // 关联菜品
        if (count > 0) {
            throw new CustomException(CustomExceptionEnum.ASSOCIATION_DISH);
        }

        // 2. 查询分类是否关联套餐
        LambdaQueryWrapper<Setmeal> lqw02 = new LambdaQueryWrapper<>();
        lqw02.eq(Setmeal::getCategoryId, id);
        int count02 = setmealService.count(lqw02);
        // 关联菜品
        if (count02 > 0) {
            throw new CustomException(CustomExceptionEnum.ASSOCIATION_SETMEAL);
        }

        // 3. 正常删除
        removeById(id);
    }
}

