package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.dto.OrderCreateDTO;
import com.snackadmin.dto.OrderQueryDTO;
import com.snackadmin.entity.Order;
import com.snackadmin.vo.OrderPageVO;
import com.snackadmin.vo.OrderVO;

/**
 * 订单服务
 */
public interface OrderService extends IService<Order> {

    OrderVO create(OrderCreateDTO dto, Long operatorId);

    OrderVO update(Long id, OrderCreateDTO dto, Long operatorId);

    OrderVO getDetail(Long id);

    Page<OrderPageVO> queryPage(OrderQueryDTO query);

    void changeStatus(Long id, String targetStatus, String remark, Long operatorId);

    void cancel(Long id, String reason, Long operatorId);

    byte[] exportExcel(OrderQueryDTO query);
}
