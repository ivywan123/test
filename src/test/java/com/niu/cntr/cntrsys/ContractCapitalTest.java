package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.*;

//追加非杠杆
public class ContractCapitalTest {
    Trade trade;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade==null){
            trade=new Trade();
        }
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(),TradeVO.getInstance().getAccountId());
    }

    @Test(groups = "open")
    //按天合约追加非杠杆100.23
    public void testContracts_capital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        float capitalAmount = 100.23f;
        Response re = func.trade_new(productId,5000,10,0);
        //获取合约信息tradeId,capitalAmount,brandId,flag,accountId
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        TradeVO.getInstance().setTradeId(tradeId);
        TradeVO.getInstance().setAccountId(accountId);
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("capitalAmount",capitalAmount); //追加100.23
        map.put("id", Action.random());

        //为断言做数据准备
        //合约借款 合约杠杆
        Integer pzMultiple = re.path("trade.pzMultiple");
        Integer borrowAmount = re.path("trade.borrowAmount");
        Integer leverCapitalAmount =  re.path("trade.leverCapitalAmount");
        Integer unlever = re.path("trade.unLeverCapitalAmount");
        float after_unlever = unlever + capitalAmount;

        Response cap = trade.contracts_capital(map);
        cap.then().body("success", equalTo(true));
        cap.then().body("capitalOrder.status", equalTo(1));
        cap.then().body("capitalOrder.preTrade.borrowAmount", is(Float.valueOf(borrowAmount)));
        cap.then().body("capitalOrder.preTrade.leverCapitalAmount", is(Float.valueOf(leverCapitalAmount)));
        cap.then().body("capitalOrder.preTrade.unLeverCapitalAmount", is(Float.valueOf(unlever)));
        cap.then().body("capitalOrder.afterTrade.borrowAmount", is(Float.valueOf(borrowAmount)));
        cap.then().body("capitalOrder.afterTrade.leverCapitalAmount", is(Float.valueOf(leverCapitalAmount)));
        cap.then().body("capitalOrder.afterTrade.unLeverCapitalAmount", is(after_unlever));

    }
}