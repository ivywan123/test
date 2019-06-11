package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
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

    @BeforeMethod
    public void setUp() {
        if(trade == null){
            trade = new Trade();
        }
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(),TradeVO.getInstance().getAccountId());
    }

    @Test(groups = "smoke")
    //按天合约放大1000
    //dataprovider
    public void testContracts_cal_leverCapital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        //获取合约信息tradeId,capitalAmount,brandId,flag,accountId
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        TradeVO.getInstance().setTradeId(tradeId);
        TradeVO.getInstance().setAccountId(accountId);
        float capitalAmount = 1000;   //放大1000
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("capitalAmount",capitalAmount);
        map.put("flag",false);
        map.put("brandId", CntrConfig.getInstance().brandId);
        //为断言做数据准备
        //合约借款 合约杠杆
        Integer pzMultiple = re.path("trade.pzMultiple");
        Integer borrowAmount = re.path("trade.borrowAmount");
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