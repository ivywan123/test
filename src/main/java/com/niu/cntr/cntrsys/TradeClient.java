package com.niu.cntr.cntrsys;

import io.restassured.response.Response;

import java.util.HashMap;

//交易相关接口
public class TradeClient extends Contact{
    //查询股票价格，用卖一价买
    public Response queryStockPrice(String stockCode, Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("stockCode",stockCode);
        map.put("brandId",brandId);
        return getResponseFromYaml("/CntrApi/queryStockPrice.yaml",map);
    }
    //查询停牌股列表
    public Response querySuspendedStkList(Long memberId, Long tradeId,Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("memberId",memberId);
        map.put("tradeId",tradeId);
        map.put("brandId",brandId);
        return getResponseFromYaml("/CntrApi/querySuspendedStkList.yaml",map);
    }
    //委托交易
    public Response sendOrder(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/sendOrder.yaml",map);
    }

    //根据股票名称查询股票价格
    public Response queryStkNm(String stkCd,Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("stkCd",stkCd);
        map.put("brandId",brandId);
        return getResponseFromYaml("/CntrApi/queryStkNm.yaml",map);
    }
}
