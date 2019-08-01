package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.func.Func;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.*;

//查询合约详情
@Test(groups = "open")
public class ContractQueryDetailTest {
    Trade trade;
    String random=String.valueOf(System.currentTimeMillis());
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade==null){
            trade=new Trade();
        }
    }

    @AfterMethod
    public void tearDown() {
    }

    @Feature("查询合约详情")
    @Description("查询合约详情-冒烟用例")
    public void testContracts_queryContractDetail() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        //获取合约信息tradeId,capitalAmount,brandId,flag,accountId
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        Response que = trade.contracts_queryContractDetail(CntrConfig.getInstance().brandId,accountId,tradeId);
        que.then().body("success",equalTo(true));
        que.then().body("trade.status",equalTo(1));
        que.then().body("trade.borrowAmount",is(5000.0f));
        que.then().body("trade.leverCapitalAmount",is(500.0f));
        que.then().body("trade.unLeverCapitalAmount",is(0.0f));
    }
}