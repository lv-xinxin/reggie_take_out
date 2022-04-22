package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;

import com.itheima.reggie.dto.OrderStatus;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;

import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page<OrdersDto>> page(int page, int pageSize, String number,
                                   Date beginTime,
                                   Date endTime) {
        LocalDateTime localDateTimeBegin = null;
        LocalDateTime localDateTimeEnd = null;
        // 对其时间参数进行处理
        if (beginTime != null && endTime != null) {
            // beginTime处理
            Instant instant = beginTime.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            localDateTimeBegin = instant.atZone(zoneId).toLocalDateTime();
            //formatBeginTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // endTime 进行处理
            Instant instant1 = endTime.toInstant();
            ZoneId zoneId1 = ZoneId.systemDefault();
            localDateTimeEnd = instant1.atZone(zoneId1).toLocalDateTime();
            //formatEndTime = localDateTime1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDto = new Page<>();

        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(number)) {
            wrapper.eq("number", number);
        }
        if (!StringUtils.isEmpty(localDateTimeBegin)) {
            wrapper.ge("order_time", localDateTimeBegin);
        }
        if (!StringUtils.isEmpty(localDateTimeEnd)) {
            wrapper.le("order_time", localDateTimeEnd);
        }
        wrapper.orderByDesc("order_time");
        ordersService.page(pageInfo, wrapper);
        // 将其除了records中的内存复制到pageDto中
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> collect = records.stream().map((order) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(order, ordersDto);
            // 根据订单id查询订单详细信息

            QueryWrapper<OrderDetail> wrapperDetail = new QueryWrapper<>();
            wrapperDetail.eq("order_id", order.getId());

            List<OrderDetail> orderDetails = orderDetailService.list(wrapperDetail);
            ordersDto.setOrderDetails(orderDetails);

            // 根据userId 查询用户姓名
            Long userId = order.getUserId();
            User user = userService.getById(userId);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());

            // 获取地址信息
            Long addressBookId = order.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());

            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);

        return R.success(pageDto);
    }

    @PutMapping
    public R<String> statusOrder(@RequestBody OrderStatus orderStatus) {
        Orders orders = ordersService.getById(orderStatus.getId());
        orders.setStatus(orderStatus.getStatus());
        ordersService.updateById(orders);
        return R.success("派送完成");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDto = new Page<>();

        ordersService.page(pageInfo);
        // 将其除了records中的内存复制到pageDto中
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> collect = records.stream().map((order) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(order, ordersDto);
            // 根据订单id查询订单详细信息

            QueryWrapper<OrderDetail> wrapperDetail = new QueryWrapper<>();
            wrapperDetail.eq("order_id", order.getId());

            List<OrderDetail> orderDetails = orderDetailService.list(wrapperDetail);
            ordersDto.setOrderDetails(orderDetails);

            // 根据userId 查询用户姓名
            Long userId = order.getUserId();
            User user = userService.getById(userId);
            if (user != null) {
                ordersDto.setUserName(user.getName());
                ordersDto.setPhone(user.getPhone());
            }


            // 获取地址信息
            Long addressBookId = order.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());

            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);
        return R.success(pageDto);
    }
}
