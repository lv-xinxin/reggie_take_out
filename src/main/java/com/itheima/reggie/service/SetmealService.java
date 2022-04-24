package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐和菜品关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    void updateStatus(int status, List<Long> ids);
}
