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

//缩小合约
public class ContractreduceCapitalTest {
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
    //按天合约缩小500
    public void testContracts_reduceCapital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        //获取合约信息tradeId,capitalAmount,brandId,flag,accountId
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        Long dataver = re.path("trade.product.datVer");
        TradeVO.getInstance().setTradeId(tradeId);
        TradeVO.getInstance().setAccountId(accountId);
        float reduceAmount = 500;   //缩小500
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("borrowAmount",reduceAmount); //缩小500
        map.put("datVer",dataver);
        map.put("id", Action.random());
        map.put("brandId", CntrConfig.getInstance().brandId);

        //为断言做数据准备
        //合约借款 合约杠杆
        Integer pzMultiple = re.path("trade.pzMultiple");
        Integer borrowAmount = re.path("trade.borrowAmount");
        Integer leverCapitalAmount =  re.path("trade.leverCapitalAmount");
        float after_borrowAmount = borrowAmount - reduceAmount;
        Double after_lever = leverCapitalAmount - Math.ceil(reduceAmount/pzMultiple);
        Double after_unlever = leverCapitalAmount - after_lever;

        Response redu = trade.contracts_reduceCapital(map);
        redu.then().body("success", equalTo(true));
        redu.then().body("reduceOrder.status", equalTo(1));
        redu.then().body("reduceOrder.preTrade.borrowAmount", is(Float.valueOf(borrowAmount)));
        redu.then().body("reduceOrder.preTrade.leverCapitalAmount", is(Float.valueOf(leverCapitalAmount)));
        redu.then().body("reduceOrder.preTrade.unLeverCapitalAmount", is(0.0f));
        redu.then().body("reduceOrder.afterTrade.borrowAmount", is(after_borrowAmount));
        redu.then().body("reduceOrder.afterTrade.leverCapitalAmount", is(Float.parseFloat(after_lever.toString())));
        redu.then().body("reduceOrder.afterTrade.unLeverCapitalAmount", is(Float.parseFloat(after_unlever.toString())));

    }
}