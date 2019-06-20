package com.niu.cntr.entity;

import java.util.Date;

public class wftransaction {
    private Long id;  //合约ID
    private Date endTradeDate;
    private Long accountId;
    private Long brandId;
    private Integer tradeId; //3.0的合约编号
    private Long productId;
    private Long productDateVer;  //产品数据版本，冗余字段

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEndTradeDate() {
        return endTradeDate;
    }

    public void setEndTradeDate(Date endTradeDate) {
        this.endTradeDate = endTradeDate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Integer getTradeId() {
        return tradeId;
    }

    public void setTradeId(Integer tradeId) {
        this.tradeId = tradeId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProductDateVer() {
        return productDateVer;
    }

    public void setProductDateVer(Long productDateVer) {
        this.productDateVer = productDateVer;
    }
}
