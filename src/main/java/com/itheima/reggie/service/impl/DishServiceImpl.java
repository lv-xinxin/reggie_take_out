package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.exceptions.CustomException;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;



    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        this.save(dishDto);

        Long id = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map(item->{
              item.setDishId(id);
              return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId() );
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());


        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map(item->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);


    }

    @Override
    public void delete(List<Long> ids) {
        // 检查菜品是否起售，若起售则不能删除
        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery(Dish.class);
        // in查询
        queryWrapper.in(Dish::getId, ids);
        // 查询起售状态的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        int count = super.count(queryWrapper);
        if (count > 0) {
            // 套餐没有停售，不能删除
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        // 检查菜品是否在套餐中，若在套餐则不能删除
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = Wrappers.lambdaQuery(SetmealDish.class);
        // in查询
        queryWrapper1.in(SetmealDish::getDishId, ids);
        // 查询起售状态的菜品
        int count1 = setmealDishService.count(queryWrapper1);
        if (count1 > 0) {
            // 菜品在套餐中，不能删除
            throw new CustomException("菜品在套餐中，不能删除");
        }
        // 更改菜品删除字段为1
        super.removeByIds(ids);
    }

    /**
     * 更新菜品状态
     *
     * @param status 菜品状态
     * @param ids    菜品id
     */
    @Override
    public void updateStatus(int status, List<Long> ids) {
        LambdaUpdateWrapper<Dish> updateWrapper = Wrappers.lambdaUpdate(Dish.class);
        updateWrapper.set(Dish::getStatus, status);
        updateWrapper.in(Dish::getId, ids);
        super.update(updateWrapper);
    }

}

























