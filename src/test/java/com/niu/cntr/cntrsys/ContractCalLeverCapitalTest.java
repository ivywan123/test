package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.hamcrest.CoreMatchers.is;

//合约放大预计算
public class ContractCalLeverCapitalTest {
    Trade trade;
    Func func = new Func();
    wftransaction wf;

    @BeforeMethod
    public void setUp() {
        if(trade == null){
            trade = new Trade();
        }
        if(wf == null){
            wf = new wftransaction();
        }
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        wf.setAccountId(re.path("trade.accountId"));
        wf.setBrandId(re.path("trade.brandId"));
        wf.setId(re.path("trade.id"));
        wf.setTradeId(Long.parseLong(re.path("trade.tradeId").toString()));
        wf.setProductDateVer(re.path("trade.product.datVer"));
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(wf.getId(), wf.getAccountId());
    }

    @Test(groups = {"open"})
    //按天合约放大1000
    //dataprovider
    public void testContracts_cal_leverCapital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        //获取合约信息tradeId,capitalAmount,brandId,flag,accountId
        Long tradeId = wf.getId();
        Long accountId = wf.getAccountId();
        wf.setId(tradeId);
        wf.setAccountId(accountId);
        float capitalAmount = 1000;   //放大1000
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("capitalAmount",capitalAmount);
        map.put("flag",false);
        map.put("brandId", wf.getBrandId());
        //为断言做数据准备
        //获取合约详情
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        Integer pzMultiple = tradeRe.path("trade.pzMultiple");
        Integer borrowAmount = tradeRe.path("trade.borrowAmount");
        Double money = capitalAmount + Math.ceil(capitalAmount/pzMultiple);
        float after_borrowAmount = borrowAmount + capitalAmount;
        //验证预计算并断言
        Response lever = trade.contracts_cal_leverCapital(map);
        lever.then().body("success",equalTo(true));
        lever.then().body("capitalOrder.money",equalTo(Float.parseFloat(money.toString())));
        lever.then().body("capitalOrder.orderType",equalTo(11003));
        lever.then().body("capitalOrder.afterTrade.borrowAmount",is(after_borrowAmount));

    }
}