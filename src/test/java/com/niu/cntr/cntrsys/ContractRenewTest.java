package com.niu.cntr.cntrsys;

import com.niu.cntr.Service.CntrDaoImpl.wftransactionServiceImpl;
import com.niu.cntr.Service.CntrService.WftransactionService;
import com.niu.cntr.Service.TradeDaoImpl.T_cntrServiceImpl;
import com.niu.cntr.Service.TradeService.T_cntrService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.redisConfig.redisUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;

//延期卖出
//合约盈利才能展期
@Test(groups = "open")
public class ContractRenewTest {
    wftransaction wf;
    Trade trade;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(wf == null){
            wf = new wftransaction();
        }
        if(trade == null){
            trade = new Trade();
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
        func.trade_delete(wf.getId(),wf.getAccountId());
    }


    //不符合操作时间
    @Feature("合约展期")
    @Description("合约展期-不符合操作时间")
    public void testContracts_renew_noTime() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",wf.getId());
        map.put("day",1);
        map.put("id", Action.random());
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        map.put("datVer",wf.getProductDateVer());
        //验证延期卖出并断言
        Response renew = trade.contracts_renew(map);
        renew.then().body("success",equalTo(false));
        renew.then().body("errCode",equalTo("500429"));
        renew.then().body("status",equalTo("false"));
        renew.then().body("resultMsg",equalTo("请在到期日当天 13:00前操作！"));
    }


    //已结算的合约延期报错
    @Feature("合约展期")
    @Description("合约展期-已结算的合约延期报错")
    public void testContracts_renew_noStatus() {
        //结算合约
        func.trade_delete(wf.getId(),wf.getAccountId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",wf.getId());
        map.put("day",1);
        map.put("id", Action.random());
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        map.put("datVer",wf.getProductDateVer());
        //验证延期卖出并断言
        Response renew = trade.contracts_renew(map);
        renew.then().body("success",equalTo(false));
        renew.then().body("errCode",equalTo("301"));
        renew.then().body("status",equalTo("false"));
        renew.then().body("resultMsg",equalTo("当前合约不存在,或与合约关联的品牌有误"));
    }


    //非盈利的合约延期报错
    @Feature("合约展期")
    @Description("合约展期-非盈利的合约延期报错")
    public void testContracts_renew_noProfit() {
        WftransactionService wftransactionService = new wftransactionServiceImpl();
        Integer result = wftransactionService.updateEndtradedate(wf);

        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",wf.getId());
        map.put("day",1);
        map.put("id", Action.random());
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        map.put("datVer",wf.getProductDateVer());
        //验证延期卖出并断言
        Response renew = trade.contracts_renew(map);
        renew.then().body("success",equalTo(false));
        renew.then().body("errCode",equalTo("500439"));
        renew.then().body("status",equalTo("false"));
        renew.then().body("resultMsg",equalTo("合约盈利情况下才能进行展期"));
    }


    //正常延期
    @Feature("合约展期")
    @Description("合约展期-正常延期")
    public void testContracts_renew_normal() {
        WftransactionService wftransactionService = new wftransactionServiceImpl();
        Integer result = wftransactionService.updateEndtradedate(wf);
        if(result != 0){
            //改造合约盈利100
            T_cntrService t_cntrService = new T_cntrServiceImpl();
            long profit = 100;
            Long cntrId = wf.getTradeId();
            t_cntrService.updateProfit(profit,cntrId);
        }
        int day=1;
        Action.sleep(30000);
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",wf.getId());
        map.put("day",day);
        map.put("id", Action.random());
        map.put("brandId",wf.getBrandId());
        map.put("accountId",wf.getAccountId());
        map.put("datVer",wf.getProductDateVer());
        //查询合约详情 借款金额，产品有偿续约公式  为断言准备数据
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        Float borrowAmount =tradeRe.path("trade.borrowAmount");
        String paidRenewCost = tradeRe.path("trade.product.paidRenewCost");
        Float pzMultiple = tradeRe.path("trade.pzMultiple");
        //获取倍率相应的有偿续约倍率
        Double[] renewPaid = {null};
        HashMap<String,Object> paidRenewCostm = Action.strTomap(paidRenewCost);
        paidRenewCostm.entrySet().forEach(entry -> {
            if(Math.abs(Float.parseFloat(entry.getKey()) - pzMultiple )>=0){
                Double d = Double.parseDouble(entry.getValue().toString());
                renewPaid[0] = borrowAmount * d * day;
            }
        });
        //验证延期卖出并断言
        Response renew = trade.contracts_renew(map);
        renew.then().body("success",equalTo(true));
        renew.then().body("renewTrans.tradeDays",equalTo(day));
        renew.then().body("renewTrans.status",equalTo(1));
        renew.then().body("renewTrans.cost",equalTo(Float.parseFloat(renewPaid[0].toString())));
        //判断合约是否限买，需要读写redis
        RedisTemplate redisTemplate= redisUtils.getRedisConnect(redisUtils.DataSourceEnvironment.cntr);
        boolean limit = redisTemplate.opsForHash().hasKey("contract:forbidden:buy","50181116102022");  //判断哈希key是否存在
        Assert.assertEquals(limit,true);  //判断合约是否限买
    }
}