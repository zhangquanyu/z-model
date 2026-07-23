package com.zmodel.dto.response;

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
public class EventDTO {

    private String id;

    private String externalFlowNo1;

    private String externalFlowNo2;

    private String pointBrandCode;

    private String sceneCode;

    private String mainOrderNo;

    private String subOrderNo;

    private LocalDateTime eventTime;

    private String partnerCode;

    private String memberCardNo;

    private String salesChannel1;

    private String salesChannel2;

    private Integer entryFlag;

    private String externalFlowNo3;

    private String businessTag;

    private String eventType;

    private BigDecimal eventAmount;

    private String pfrId;

    private String operator;

    private String remark;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
