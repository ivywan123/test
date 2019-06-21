package com.niu.cntr.cntrsys;

import com.niu.cntr.Service.CntrDaoImpl.brandServiceImpl;
import com.niu.cntr.Service.CntrDaoImpl.wftransactionServiceImpl;
import com.niu.cntr.Service.CntrService.BrandService;
import com.niu.cntr.Service.CntrService.WftransactionService;
import com.niu.cntr.Service.TradeDaoImpl.RoleServiceImpl;
import com.niu.cntr.Service.TradeDaoImpl.T_cntrServiceImpl;
import com.niu.cntr.Service.TradeService.RoleService;
import com.niu.cntr.Service.TradeService.T_cntrService;
import com.niu.cntr.entity.brand;
import com.niu.cntr.entity.role;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.inspect.SqlConnect;
import com.niu.cntr.redisConfig.redisUtils;
import io.restassured.response.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

//延期卖出
//合约盈利才能展期
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
        wf.setTradeId(re.path("trade.tradeId"));
        wf.setProductDateVer(re.path("trade.product.datVer"));
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(wf.getId(),wf.getAccountId());
    }

    @Test(groups = "smoke")
    //不符合操作时间
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
        renew.then().body("resultMsg",equalTo("请在到期日当天 23:59前操作！"));
    }

    @Test(groups = "smoke")
    //已结算的合约延期报错
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

    @Test(groups = "smoke")
    //非盈利的合约延期报错
    public void testContracts_renew_noProfit() {
        RedisTemplate redisTemplate= redisUtils.getRedisConnect(redisUtils.DataSourceEnvironment.cntr);
        redisTemplate.opsForValue().set("test","11111");
        redisTemplate.opsForValue().get("test");
        RedisTemplate redisTemplate2= redisUtils.getRedisConnect(redisUtils.DataSourceEnvironment.trade);
        redisTemplate2.opsForValue().set("test","22222");
        redisTemplate2.opsForValue().get("test");
        /**
        BrandService BrandService = new brandServiceImpl();
        List<brand> list = BrandService.findAll();
        RoleService roleService = new RoleServiceImpl();
        List<role> rolelist = roleService.findAll();
        **/
        WftransactionService wftransactionService = new wftransactionServiceImpl();
        wf.setEndTradeDate(new Date());
        Integer result = wftransactionService.updateEndtradedate(wf);
        if(result == 0){
            System.out.println("更新合约信息失败");
            return;
        }
        /**
        SqlConnect sc = new SqlConnect();
        Long tradeId = TradeVO.getInstance().getTradeId();
        //修改合约到期时间（当日）
        sc.update("cntrsys","update wftransaction set endTradeDate ='"+ Action.Time() +"' where id ="+tradeId+";");
        **/

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

    @Test(groups = "smoke")
    //正常延期
    public void testContracts_renew_normal() {

        WftransactionService wftransactionService = new wftransactionServiceImpl();
        wf.setEndTradeDate(new Date());
        Integer result = wftransactionService.updateEndtradedate(wf);
        if(result != 0){
            //改造合约盈利100
            T_cntrService t_cntrService = new T_cntrServiceImpl();
            BigDecimal profit = new BigDecimal(100);
            Long cntrId = wf.getTradeId();
            t_cntrService.updateProfit(profit,cntrId);
        }
        int day=1;
        //修改合约到期时间（当日）
//        sc.update("cntrsys","update wftransaction set endTradeDate ='"+ Action.Time() +"' where id ="+tradeId+";");
        //改造合约盈利100
//        sc.update("niudb","UPDATE t_cntr SET Cur_Bal_Amt =Cur_Bal_Amt+100,Cur_Aval_Cap_Amt=Cur_Aval_Cap_Amt+100,Cur_Tt_Ast_Amt=Cur_Tt_Ast_Amt+100 WHERE Cntr_Id ="+ TradeVO.getInstance().getCntrId()+";");
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
        //todo:还需判断合约是否限买，需要读写redis
    }
}