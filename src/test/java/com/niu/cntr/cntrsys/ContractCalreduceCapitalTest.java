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
import static org.testng.Assert.*;

//缩小合约预计算用例
public class ContractCalreduceCapitalTest {
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

    @Test(groups = "open")
    //按天合约缩小500
    //todo:dataprovider
    public void testContracts_cal_reduceCapital() {
        HashMap<String, Object> map = new HashMap<>();
        //获取合约信息tradeId,borrowAmount,brandId,accountId
        Long tradeId = wf.getId();
        Long accountId = wf.getAccountId();
        wf.setId(tradeId);
        wf.setAccountId(accountId);
        float reduceAmount = 500;   //缩小500
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("borrowAmount",reduceAmount);  //缩小500
        map.put("brandId", wf.getBrandId());
        //为断言做数据准备
        //获取合约详情
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        Integer pzMultiple = tradeRe.path("trade.pzMultiple");
        Integer borrowAmount = tradeRe.path("trade.borrowAmount");
        Integer leverCapitalAmount =  tradeRe.path("trade.leverCapitalAmount");
        float after_borrowAmount = borrowAmount - reduceAmount;
        Double after_lever = leverCapitalAmount - Math.ceil(reduceAmount/pzMultiple);
        Double after_unlever = leverCapitalAmount - after_lever;

        Response redu = trade.contracts_cal_reduceCapital(map);
        redu.then().body("success",equalTo(true));
        redu.then().body("reduceOrder.status",equalTo(0));
        redu.then().body("reduceOrder.preTrade.borrowAmount",is(Float.valueOf(borrowAmount)));
        redu.then().body("reduceOrder.preTrade.leverCapitalAmount",is(Float.valueOf(leverCapitalAmount)));
        redu.then().body("reduceOrder.preTrade.unLeverCapitalAmount",is(0.0f));
        redu.then().body("reduceOrder.afterTrade.borrowAmount",is(after_borrowAmount));
        redu.then().body("reduceOrder.afterTrade.leverCapitalAmount",is(Float.parseFloat(after_lever.toString())));
        redu.then().body("reduceOrder.afterTrade.unLeverCapitalAmount",is(Float.parseFloat(after_unlever.toString())));
    }
}