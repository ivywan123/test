package com.niu.cntr.cntrsys;

import com.niu.cntr.Service.CntrDaoImpl.wftransactionServiceImpl;
import com.niu.cntr.Service.CntrService.WftransactionService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.inspect.SqlConnect;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.Assert.*;

//合约展期预计算天数
@Test(groups = "open")
public class ContractCalRenewpaidDayTest {
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
        //准备待测合约
        //新增一个财云免息T+1的合约
        String productId = "52825297527251";
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


    public void testContracts_cal_renew_paidDay_noTime() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",wf.getId());
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        //验证预计算并断言
        Response paidcash = trade.contracts_cal_renew_paidDay(map);
        paidcash.then().body("success",equalTo(false));
        paidcash.then().body("errCode",equalTo("500429"));
        paidcash.then().body("status",equalTo("false"));
        paidcash.then().body("resultMsg",equalTo("请在到期日当天 13:00前操作！"));

    }


    public void testContracts_cal_renew_paidDay_normal() {
        HashMap<String, Object> map = new HashMap<>();
        Long tradeId = wf.getId();
        //修改合约到期时间（当日）
        WftransactionService wftransactionService = new wftransactionServiceImpl();
        Integer result = wftransactionService.updateEndtradedate(wf);

        map.put("tradeId",tradeId);
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        //查询合约详情 wfDuration，产品最大生存周期maxDuration
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),tradeId);
        int wfDuration =tradeRe.path("trade.wfDuration");
        int maxDuration = tradeRe.path("trade.product.maxDuration");
        //验证预计算并断言
        Response paidDay = trade.contracts_cal_renew_paidDay(map);
        paidDay.then().body("success",equalTo(true));
        paidDay.then().body("tradeDays",equalTo(maxDuration - wfDuration));
    }
}