package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequest {

    @NotBlank(message = "外部流水号一级不能为空")
    @Size(max = 100, message = "外部流水号一级长度不能超过100个字符")
    private String externalFlowNo1;

    @Size(max = 100, message = "外部流水号二级长度不能超过100个字符")
    private String externalFlowNo2;

    @Size(max = 50, message = "积分品牌代码长度不能超过50个字符")
    private String pointBrandCode;

    @Size(max = 50, message = "场景码长度不能超过50个字符")
    private String sceneCode;

    @Size(max = 100, message = "主订单号长度不能超过100个字符")
    private String mainOrderNo;

    @Size(max = 100, message = "子订单号长度不能超过100个字符")
    private String subOrderNo;

    private LocalDateTime eventTime;

    @Size(max = 50, message = "合作伙伴代码长度不能超过50个字符")
    private String partnerCode;

    @Size(max = 50, message = "会员卡号长度不能超过50个字符")
    private String memberCardNo;

    @Size(max = 50, message = "销售渠道一级长度不能超过50个字符")
    private String salesChannel1;

    @Size(max = 50, message = "销售渠道二级长度不能超过50个字符")
    private String salesChannel2;

    private Integer entryFlag;

    @Size(max = 100, message = "外部流水号三级长度不能超过100个字符")
    private String externalFlowNo3;

    @Size(max = 100, message = "业务标签长度不能超过100个字符")
    private String businessTag;

    @Size(max = 50, message = "事件类型长度不能超过50个字符")
    private String eventType;

    private BigDecimal eventAmount;

    @Size(max = 100, message = "PFRID长度不能超过100个字符")
    private String pfrId;

    @Size(max = 50, message = "操作人长度不能超过50个字符")
    private String operator;

    @Size(max = 5000, message = "备注长度不能超过5000个字符")
    private String remark;
}
