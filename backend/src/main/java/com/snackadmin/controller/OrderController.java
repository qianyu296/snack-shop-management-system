package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.OrderCreateDTO;
import com.snackadmin.dto.OrderQueryDTO;
import com.snackadmin.dto.OrderStatusChangeDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.OrderService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.OrderPageVO;
import com.snackadmin.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SysUserService sysUserService;

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderPageVO>> list(@Valid OrderQueryDTO query) {
        Page<OrderPageVO> page = orderService.queryPage(query);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.getDetail(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto, Principal principal) {
        Long operatorId = getOperatorId(principal);
        OrderVO vo = orderService.create(dto, operatorId);
        return Result.ok("创建成功", vo);
    }

    @Operation(summary = "编辑订单（仅待确认状态）")
    @PutMapping("/{id}")
    public Result<OrderVO> update(@PathVariable Long id, @Valid @RequestBody OrderCreateDTO dto,
                                   Principal principal) {
        Long operatorId = getOperatorId(principal);
        OrderVO vo = orderService.update(id, dto, operatorId);
        return Result.ok("修改成功", vo);
    }

    @Operation(summary = "变更订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody OrderStatusChangeDTO dto,
                                      Principal principal) {
        Long operatorId = getOperatorId(principal);
        orderService.changeStatus(id, dto.getTargetStatus(), dto.getRemark(), operatorId);
        return Result.ok();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String reason,
                                Principal principal) {
        Long operatorId = getOperatorId(principal);
        orderService.cancel(id, reason, operatorId);
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var user = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return user != null ? user.getId() : null;
    }
}
