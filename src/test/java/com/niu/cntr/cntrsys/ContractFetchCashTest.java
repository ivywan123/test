package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
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

//提取现金
@Test(groups = "close")
public class ContractFetchCashTest {
    Trade trade;
    Func func = new Func();
    wftransaction wf;

    @BeforeMethod
    public void setUp() {
        if (trade == null) {
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
        func.trade_delete(wf.getId(),wf.getAccountId());
    }


    //缩小合约，提取5.36
    public void testContracts_fetchCash() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        //缩小合约产生非杠杆
        Response redu = func.trade_reduce(wf.getId(), wf.getAccountId(), 1000);  //非杠杆为100
        //提取现金
        float cash = 5.36f;
        map.put("id", Action.random());
        map.put("flag", true);  //可提取利润和现金
        map.put("accountId", wf.getAccountId());
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("cash", cash);  //提取小于可提现金的金额，可提现金为100
        map.put("tradeId", wf.getId());
        //为断言做数据准备
        //合约借款 合约杠杆
        float wfPercent = redu.path("reduceOrder.afterTrade.wfPercent");
        float unLeverCapitalAmount = redu.path("reduceOrder.afterTrade.unLeverCapitalAmount");
        Response fetch = trade.contracts_fetchCash(map);
        fetch.then().body("success", equalTo(true));
        fetch.then().body("fetchCashOrder.status", equalTo(1));
        fetch.then().body("fetchCashOrder.afterTrade.wfPercent", is(wfPercent - cash));
        fetch.then().body("fetchCashOrder.afterTrade.unLeverCapitalAmount", is(unLeverCapitalAmount - cash));

    }
}