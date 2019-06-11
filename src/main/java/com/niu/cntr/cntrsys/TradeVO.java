package com.niu.cntr.cntrsys;

public class TradeVO {
    private Long tradeId;
    private Long accountId;
    private Long brandId;
    private Integer cntrId;
    private Long productId;
    private Long dataVer;  //产品数据版本

    private static TradeVO tradeVO;
    private TradeVO(){}
    public static TradeVO getInstance(){
        if(tradeVO==null){
            tradeVO = new TradeVO();
        }
        return tradeVO;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
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

    public Integer getCntrId() {
        return cntrId;
    }

    public void setCntrId(Integer cntrId) {
        this.cntrId = cntrId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getDataVer() {
        return dataVer;
    }

    public void setDataVer(Long dataVer) {
        this.dataVer = dataVer;
    }
}
