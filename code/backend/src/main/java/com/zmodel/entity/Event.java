package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_flow_no_1", nullable = false, length = 100)
    private String externalFlowNo1;

    @Column(name = "external_flow_no_2", length = 100)
    private String externalFlowNo2;

    @Column(name = "point_brand_code", length = 50)
    private String pointBrandCode;

    @Column(name = "scene_code", length = 50)
    private String sceneCode;

    @Column(name = "main_order_no", length = 100)
    private String mainOrderNo;

    @Column(name = "sub_order_no", length = 100)
    private String subOrderNo;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "partner_code", length = 50)
    private String partnerCode;

    @Column(name = "member_card_no", length = 50)
    private String memberCardNo;

    @Column(name = "sales_channel_1", length = 50)
    private String salesChannel1;

    @Column(name = "sales_channel_2", length = 50)
    private String salesChannel2;

    @Column(name = "entry_flag")
    private Integer entryFlag;

    @Column(name = "external_flow_no_3", length = 100)
    private String externalFlowNo3;

    @Column(name = "business_tag", length = 100)
    private String businessTag;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "event_amount", precision = 18, scale = 2)
    private BigDecimal eventAmount;

    @Column(name = "pfr_id", length = 100)
    private String pfrId;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
