package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fo_node")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoNode {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "orchestration_id", nullable = false, length = 36)
    private String orchestrationId;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "node_type", nullable = false, length = 30)
    private String nodeType;

    @Column(name = "node_name", length = 200)
    private String nodeName;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "loop_count")
    private Integer loopCount;

    @Column(name = "condition_expression", length = 500)
    private String conditionExpression;

    @Column(name = "db_operation", length = 20)
    private String dbOperation;

    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "api_url", length = 500)
    private String apiUrl;

    @Column(name = "api_method", length = 10)
    private String apiMethod;

    @Column(name = "transform_type", length = 30)
    private String transformType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "position_x")
    private Double x;

    @Column(name = "position_y")
    private Double y;

    @Column(name = "physical_model_id", length = 36)
    private String physicalModelId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
