package com.htecoin.flinkdemo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetSlip {
    @Id
    private Long id;
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Date createdDate;

    @CreatedBy
    @Column(length = 32)
    private String createdBy;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    @Column(length = 32)
    private String lastModifiedBy;
    private String gameCode;
    private BigDecimal odds;
    private BigDecimal wagerAmount;
    private BigDecimal initWagerAmount;
    private BigDecimal payoffAmount;
    private BigDecimal returnAmount;
    private Long playerId;
}
