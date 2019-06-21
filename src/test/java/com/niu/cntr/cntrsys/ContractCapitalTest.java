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

//追加非杠杆
//1、正常追加
//2、追加金额不能小于总操盘资金1%
//3、在途的操作（修改redis）
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
        wf.setTradeId(re.path("trade.tradeId"));
        wf.setProductDateVer(re.path("trade.product.datVer"));
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
        float capitalAmount = 100.23f;
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("brandId", wf.getBrandId());
        map.put("capitalAmount",capitalAmount); //追加100.23
        map.put("id", Action.random());

        //为断言做数据准备
        //合约借款 合约杠杆
        Response re = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
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

    @Test(groups = "open")
    //按天合约追加非杠杆100.23
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
        cap.then().body("errCode", equalTo(500411));
        cap.then().body("resultMsg", equalTo("追加保证金不能小于当前方案总操盘资金的1%"));
    }

}