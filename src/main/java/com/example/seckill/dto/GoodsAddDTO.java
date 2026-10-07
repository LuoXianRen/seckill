package com.example.seckill.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 新增秒杀商品请求参数。
 *
 * @author example
 */
@Data
public class GoodsAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（新增时由 MyBatis useGeneratedKeys 回填）
     */
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    private String goodsTitle;

    private String goodsImg;

    private String goodsDetail;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal goodsPrice;

    @NotNull(message = "秒杀价不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价必须大于0")
    private BigDecimal seckillPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    private Integer stockCount;

    /**
     * 开始/结束时间：前端传时间戳（毫秒），便于 JDK8 下处理
     */
    @NotNull(message = "开始时间不能为空")
    private Long startTimestamp;

    @NotNull(message = "结束时间不能为空")
    private Long endTimestamp;

    public Date getStartDate() {
        return new Date(startTimestamp);
    }

    public Date getEndDate() {
        return new Date(endTimestamp);
    }
}
