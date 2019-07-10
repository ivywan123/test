package com.niu.cntr.cntrsys;

import com.niu.cntr.Service.TradeDaoImpl.T_cntrServiceImpl;
import com.niu.cntr.Service.TradeService.T_cntrService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.redisConfig.redisUtils;
import io.restassured.response.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

//放大合约测试用例
//1、资金池不满足
//2、操盘中状态才可放大（已结束合约放大）
//3、放大借款<可申请范围 小于1000
//4、放大借款不为千的整数倍

//5、不使用可提现金放大，盘中
//6、使用可提现金（利润+非杠杆）放大，盘后，有可提，需验证合约累计盈亏=利润  无同步市值接口
//7、无可提，使用可提现金放大，盘后 ok
//8、补亏的放大

//当前产品无放大合约功能
public class ContractleverCapitalTest {
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

    }

    @AfterMethod
    public void tearDown() {
        //终止合约
        func.trade_delete(wf.getId(),wf.getAccountId());
    }

    @BeforeGroups(groups = { "lever" })
    //为前面的用例创造一个按天合约
    public void CreateDay(){
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        wf.setAccountId(re.path("trade.accountId"));
        wf.setBrandId(re.path("trade.brandId"));
        wf.setId(re.path("trade.id"));
        wf.setTradeId(Long.parseLong(re.path("trade.tradeId").toString()));
        wf.setProductDateVer(re.path("trade.product.datVer"));
    }

    @Test(groups = {"open","lever"})
    //1、资金池不满足
    public void testContracts_leverCapital_NoCatital(){
        //查询合约详情，获取资金池
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        int fundPoolId = tradeRe.path("trade.fundPoolId");
        //获取资金池的redis值
        RedisTemplate redisTemplate= redisUtils.getRedisConnect(redisUtils.DataSourceEnvironment.trade);
        Double catital = Double.parseDouble(redisTemplate.opsForHash().get("capital",fundPoolId).toString());  //获取哈希key
        //设置redis值为一个不够的值
        redisTemplate.opsForHash().increment("capital",fundPoolId,100);
        //放大合约，报错
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 1000f;   //放大1000
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大1000
        map.put("flag",false);  //不使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        redisTemplate.opsForHash().increment("capital",fundPoolId,catital);
        try {
            Response lever = trade.contracts_leverCapital(map);
            lever.then().body("success", equalTo(false));
        }finally {
            //重置redis值
            redisTemplate.opsForHash().increment("capital",fundPoolId,catital);
        }
    }

    @Test(groups = {"open","lever"})
    //2、已结束合约放大
    public void testContracts_leverCapital_close(){
        //结算合约
        func.trade_delete(wf.getId(),wf.getAccountId());
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 1000f;   //放大1000
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大1000
        map.put("flag",false);  //不使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        Response lever = trade.contracts_leverCapital(map);
        lever.then().body("success", equalTo(false));
    }

    @Test(groups = {"open","lever"})
    //3、放大借款<可申请范围 小于1000  大于5000000
    public void testContracts_leverCapital_out() {
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 100f;   //放大100
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大100
        map.put("flag",false);  //不使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        Response lever = trade.contracts_leverCapital(map);
        lever.then().body("success", equalTo(false));
        map.put("capitalAmount",6000000f);  //放大6000000
        Response lever2 = trade.contracts_leverCapital(map);
        lever2.then().body("success", equalTo(false));
    }

    //4、放大借款不为千的整数倍
    @Test(groups = {"open","lever"})
    public void testContracts_leverCapital_Nomoney() {
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 1100f;   //放大1100
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大1100
        map.put("flag",false);  //不使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        Response lever = trade.contracts_leverCapital(map);
        lever.then().body("success", equalTo(false));
    }

    @Test(groups = {"open","lever"})
    //5、不使用可提现金，按天合约放大1000
    //todo:dataprovider
    public void testContracts_leverCapital() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        BigDecimal capitalAmount = new BigDecimal(1000);   //放大1000
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大1000
        map.put("flag",false);  //不使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        //为断言做数据准备
        //获取合约详情
        Response tradeRe = func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId());
        //合约借款 合约杠杆
        BigDecimal pzMultiple = tradeRe.path("trade.pzMultiple");
        BigDecimal borrowAmount = tradeRe.path("trade.borrowAmount");
        BigDecimal money = capitalAmount.add(capitalAmount.divide(pzMultiple,0,BigDecimal.ROUND_HALF_UP));
        BigDecimal after_borrowAmount = borrowAmount.add(capitalAmount);
        //验证放大并断言
        Response lever = trade.contracts_leverCapital(map);
        lever.then().body("success", equalTo(true));
        lever.then().body("capitalOrder.money", equalTo(money));
        lever.then().body("capitalOrder.orderType", equalTo(11003));
        lever.then().body("capitalOrder.afterTrade.borrowAmount", is(after_borrowAmount));

    }

    //6、使用可提现金（利润+非杠杆）放大，盘后，有可提，需验证合约累计盈亏=利润  无同步市值接口
    @Test(groups = {"close","lever"})
    public void testContracts_leverCapital_flag1() {
        //合约追加非杠杆
        func.trade_Capital(wf.getId(),wf.getAccountId(),1000);
        //添加利润
        //改造合约盈利100
        T_cntrService t_cntrService = new T_cntrServiceImpl();
        long profit = 100;
        Long cntrId = wf.getTradeId();
        t_cntrService.updateProfit(profit,cntrId);
        Action.sleep(30000);
        //放大
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 3000f;   //放大1000
        map.put("tradeId",wf.getId());
        map.put("accountId",wf.getAccountId());
        map.put("capitalAmount",capitalAmount); //放大3000，10倍，杠杆本金增加300，使用利润100，非杠杆200
        map.put("flag",true);  //使用可提现金
        map.put("datVer",wf.getProductDateVer());
        map.put("id", Action.random());
        map.put("brandId", wf.getBrandId());
        //验证放大并断言
        Response lever = trade.contracts_leverCapital(map);
        lever.then().body("success", equalTo(true));
        lever.then().body("capitalOrder.unLeverSubAmount", equalTo(200));
        lever.then().body("capitalOrder.profitAmount", equalTo(profit));
        lever.then().body("capitalOrder.orderType", equalTo(11003));
        lever.then().body("capitalOrder.status", equalTo(1));
    }

    //8、补亏的放大  总资产-借款-杠杆<0
    //
}