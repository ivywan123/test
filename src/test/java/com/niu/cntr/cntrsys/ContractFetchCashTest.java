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

//提取现金
public class ContractFetchCashTest {
    Trade trade;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if (trade == null) {
            trade = new Trade();
        }
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(), TradeVO.getInstance().getAccountId());
    }

    @Test(groups = "close")
    //缩小合约，提取5.36
    public void testContracts_fetchCash() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        Response re = func.trade_new(productId, 5000, 10, 0);
        //缩小合约产生非杠杆
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        TradeVO.getInstance().setTradeId(tradeId);
        TradeVO.getInstance().setAccountId(accountId);
        Response redu = func.trade_reduce(tradeId, accountId, 1000);  //非杠杆为100
        //提取现金
        float cash = 5.36f;
        map.put("id", Action.random());
        map.put("flag", true);  //可提取利润和现金
        map.put("accountId", accountId);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("cash", cash);  //提取小于可提现金的金额，可提现金为100
        map.put("tradeId", tradeId);

        //为断言做数据准备
        //合约借款 合约杠杆
        float wfPercent = redu.path("reduceOrder.afterTrade.wfPercent");
        float unLeverCapitalAmount = redu.path("reduceOrder.afterTrade.unLeverCapitalAmount");
//        float after_wfper = wfPercent - cash;
//        float after_unlever = unLeverCapitalAmount - cash;

        Response fetch = trade.contracts_fetchCash(map);
        fetch.then().body("success", equalTo(true));
        fetch.then().body("fetchCashOrder.status", equalTo(1));
        fetch.then().body("fetchCashOrder.afterTrade.wfPercent", is(wfPercent - cash));
        fetch.then().body("fetchCashOrder.afterTrade.unLeverCapitalAmount", is(unLeverCapitalAmount - cash));

    }
}