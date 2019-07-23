package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.*;

//分组，按天合约
//1、冒烟用例，普通按天合约缩小500
//2、合约状态检查（已结束合约缩小）
//3、无持仓无委托缩小到0，校验借款和杠杆是否为0
//4、有持仓，全部缩小，报错：缩小金额不能大于借贷或可用
//5、最高可缩小>=100，缩小部分，金额必须为整百：最高可缩小2333，如果缩小2330，是不允许的。要么缩完，要么是整百倍数 反例
//6、最高可缩小>=100，有零头，一次性缩小，正例
//7、
//分组，按周按月，必须到期日当天才能操作

//缩小合约
@Test(groups = "open")
public class ContractreduceCapitalTest {
    Trade trade;
    Func func = new Func();
    wftransaction wf;

    @BeforeMethod
    public void setUp() {
        if(trade==null){
            trade=new Trade();
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

    //按天合约缩小500
    @Test(groups = "open")
    public void testContracts_reduceCapital() {
        HashMap<String, Object> map = new HashMap<>();
        BigDecimal reduceAmount = new BigDecimal(500);   //缩小500
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("borrowAmount",reduceAmount); //缩小500
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());

        //为断言做数据准备
        //获取合约详情
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        //合约借款 合约杠杆
        BigDecimal pzMultiple = new BigDecimal(tradeRe.path("trade.pzMultiple").toString());
        BigDecimal borrowAmount = new BigDecimal(tradeRe.path("trade.borrowAmount").toString());
        BigDecimal leverCapitalAmount = new BigDecimal(tradeRe.path("trade.leverCapitalAmount").toString());
        BigDecimal after_borrowAmount = borrowAmount.subtract(reduceAmount);
        BigDecimal after_lever = leverCapitalAmount.subtract(reduceAmount.divide(pzMultiple,0,BigDecimal.ROUND_HALF_UP));
        BigDecimal after_unlever = leverCapitalAmount.subtract(after_lever);

        Response redu = trade.contracts_reduceCapital(map);
        redu.then().body("success", equalTo(true));
        redu.then().body("reduceOrder.status", equalTo(1));
        redu.then().body("reduceOrder.preTrade.borrowAmount",equalTo(Float.parseFloat(borrowAmount.toString())));
        redu.then().body("reduceOrder.preTrade.leverCapitalAmount",equalTo(Float.parseFloat(leverCapitalAmount.toString())));
        redu.then().body("reduceOrder.preTrade.unLeverCapitalAmount",is(0.0f));
        redu.then().body("reduceOrder.afterTrade.borrowAmount",equalTo(Float.parseFloat(after_borrowAmount.toString())));
        redu.then().body("reduceOrder.afterTrade.leverCapitalAmount",equalTo(Float.parseFloat(after_lever.toString())));
        redu.then().body("reduceOrder.afterTrade.unLeverCapitalAmount",equalTo(Float.parseFloat(after_unlever.toString())));

    }

    //2、合约状态检查（已结束合约缩小）
    @Test(groups = "open")
    public void testContracts_reduceCapital_close() {
        //结算合约
        func.trade_delete(wf.getId(),wf.getAccountId());
        HashMap<String, Object> map = new HashMap<>();
        float reduceAmount = 500;   //缩小500
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("borrowAmount",reduceAmount); //缩小500
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        Response redu = trade.contracts_reduceCapital(map);
        redu.then().body("success", equalTo(false));
    }

    //3、无持仓无委托缩小到0，校验借款和杠杆是否为0
    @Test(groups = "open")
    public void testContracts_reduceCapital_zero(){
        HashMap<String, Object> map = new HashMap<>();
        //查询合约详情，确定缩小金额为借款金额
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        BigDecimal borrowAmount = new BigDecimal(tradeRe.path("trade.borrowAmount").toString());
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("borrowAmount",borrowAmount); //缩小借款，即为全部缩小
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        Response redu = trade.contracts_reduceCapital(map);
        redu.then().body("reduceOrder.status", equalTo(1));
        redu.then().body("reduceOrder.afterTrade.borrowAmount", equalTo(0.0f));
        redu.then().body("reduceOrder.afterTrade.leverCapitalAmount", equalTo(0.0f));
    }


}