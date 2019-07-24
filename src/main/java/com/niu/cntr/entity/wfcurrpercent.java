package com.niu.cntr.entity;

import java.math.BigDecimal;

public class wfcurrpercent {
    private Long id;  //合约ID
    private BigDecimal assetValue;
    private BigDecimal currentCash;
    private BigDecimal marketValue;
    private BigDecimal currentBalance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAssetValue() {
        return assetValue;
    }

    public void setAssetValue(BigDecimal assetValue) {
        this.assetValue = assetValue;
    }

    public BigDecimal getCurrentCash() {
        return currentCash;
    }

    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
}
