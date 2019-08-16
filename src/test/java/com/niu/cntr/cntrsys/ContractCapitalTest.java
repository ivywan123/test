package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.*;

//追加非杠杆
//1、正常追加
//2、追加金额不能小于总操盘资金1%
//3、在途的操作（修改redis）
@Test(groups = "open")
public class ContractCapitalTest {
    wftransaction wf;
    Trade trade;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade==null){
            trade=new Trade();
        }
        if(wf == null){
            wf = new wftransaction();
        }
        //准备待测合约
        //新增一个财云按天的合约
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


    @Feature("追加非杠杆")
    @Story("追加非杠杆-正例")
    @Description("追加非杠杆-正例")
    //按天合约追加非杠杆100.23
    public void testContracts_capital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        BigDecimal capitalAmount = new BigDecimal(100.23);
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("brandId", wf.getBrandId());
        map.put("capitalAmount",capitalAmount); //追加100.23
        map.put("id", Action.random());

        //为断言做数据准备
        //合约借款 合约杠杆
        Response re = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        BigDecimal pzMultiple = new BigDecimal(re.path("trade.pzMultiple").toString());
        BigDecimal borrowAmount = new BigDecimal(re.path("trade.borrowAmount").toString());
        BigDecimal leverCapitalAmount = new BigDecimal(re.path("trade.leverCapitalAmount").toString());
        BigDecimal unlever = new BigDecimal(re.path("trade.unLeverCapitalAmount").toString());
        BigDecimal after_unlever = unlever.add(capitalAmount);

        Response cap = trade.contracts_capital(map);
        cap.then().body("success", equalTo(true));
        cap.then().body("capitalOrder.status", equalTo(1));
        cap.then().body("capitalOrder.preTrade.borrowAmount", equalTo(Float.parseFloat(borrowAmount.toString())));
        cap.then().body("capitalOrder.preTrade.leverCapitalAmount", equalTo(Float.parseFloat(leverCapitalAmount.toString())));
        cap.then().body("capitalOrder.preTrade.unLeverCapitalAmount", equalTo(Float.parseFloat(unlever.toString())));
        cap.then().body("capitalOrder.afterTrade.borrowAmount", equalTo(Float.parseFloat(borrowAmount.toString())));
        cap.then().body("capitalOrder.afterTrade.leverCapitalAmount", equalTo(Float.parseFloat(leverCapitalAmount.toString())));
        cap.then().body("capitalOrder.afterTrade.unLeverCapitalAmount", equalTo(Float.parseFloat(after_unlever.toString())));

    }

    @Feature("追加非杠杆")
    @Story("追加非杠杆-反例，小于总操盘1%")
    @Description("追加非杠杆-反例，小于总操盘1%")
    //按天合约追加非杠杆0.23
    public void testContracts_capital_no() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 0.23f;
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("brandId", wf.getBrandId());
        map.put("capitalAmount",capitalAmount); //追加0.23
        map.put("id", Action.random());

        Response cap = trade.contracts_capital(map);
        cap.then().body("success", equalTo(false));
        cap.then().body("status", equalTo("false"));
        cap.then().body("errCode", equalTo("500411"));
        cap.then().body("resultMsg", equalTo("追加保证金不能小于当前方案总操盘资金的1%"));
    }

}