package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fo_node_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoNodeConfig {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "node_id", nullable = false, length = 36)
    private String nodeId;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_type", length = 20)
    @Builder.Default
    private String configType = "STRING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
