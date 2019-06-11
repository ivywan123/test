package com.niu.cntr.cntrsys;

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
public class ContractCalRenewpaidDayTest {
    Trade trade;
    String random=String.valueOf(System.currentTimeMillis());
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade == null){
            trade = new Trade();
        }
        //准备待测合约
        //新增一个财云免息T+1的合约
        String productId = "52825297527251";
        Response re = func.trade_new(productId,5000,10,0);
        TradeVO.getInstance().setAccountId(re.path("trade.accountId"));
        TradeVO.getInstance().setBrandId(re.path("trade.brandId"));
        TradeVO.getInstance().setTradeId(re.path("trade.id"));
        TradeVO.getInstance().setCntrId(re.path("trade.tradeId"));
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(),TradeVO.getInstance().getAccountId());
    }

    @Test(groups = "smoke")
    public void testContracts_cal_renew_paidDay_noTime() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",TradeVO.getInstance().getTradeId());
        map.put("brandId",TradeVO.getInstance().getBrandId());
        map.put("accountId",TradeVO.getInstance().getAccountId());
        //验证预计算并断言
        Response paidcash = trade.contracts_cal_renew_paidDay(map);
        paidcash.then().body("success",equalTo(false));
        paidcash.then().body("errCode",equalTo("500429"));
        paidcash.then().body("status",equalTo("false"));
        paidcash.then().body("resultMsg",equalTo("请在到期日当天 23:59前操作！"));

    }

    @Test(groups = "smoke")
    public void testContracts_cal_renew_paidDay_normal() {
        HashMap<String, Object> map = new HashMap<>();
        SqlConnect sc = new SqlConnect();
        Long tradeId = TradeVO.getInstance().getTradeId();
        //修改合约到期时间（当日）
        sc.update("cntrsys","update wftransaction set endTradeDate ='"+ Action.Time() +"' where id ="+tradeId+";");
        map.put("tradeId",TradeVO.getInstance().getTradeId());
        map.put("brandId",TradeVO.getInstance().getBrandId());
        map.put("accountId",TradeVO.getInstance().getAccountId());
        //查询合约详情 wfDuration，产品最大生存周期maxDuration
        Response tradeRe = func.queryTrade(TradeVO.getInstance().getBrandId(),TradeVO.getInstance().getAccountId(),tradeId);
        int wfDuration =tradeRe.path("trade.wfDuration");
        int maxDuration = tradeRe.path("trade.product.maxDuration");
        //验证预计算并断言
        Response paidDay = trade.contracts_cal_renew_paidDay(map);
        paidDay.then().body("success",equalTo(true));
        paidDay.then().body("tradeDays",equalTo(maxDuration - wfDuration));
    }
}