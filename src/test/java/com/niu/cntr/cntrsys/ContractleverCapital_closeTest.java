package com.niu.cntr.cntrsys;

import com.niu.cntr.Service.CntrDaoImpl.wfcurrpercentServiceImpl;
import com.niu.cntr.Service.CntrService.WfcurrpercentService;
import com.niu.cntr.Service.TradeDaoImpl.T_cntrServiceImpl;
import com.niu.cntr.Service.TradeService.T_cntrService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.equalTo;

@Test(groups = {"close"})
public class ContractleverCapital_closeTest {
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
        //终止合约
        func.trade_delete(wf.getId(),wf.getAccountId());
    }

    @Feature("放大合约")
    @Story("可提现金放大-盘后")
    @Description("可提现金放大-盘后")
    //6、使用可提现金（利润+非杠杆）放大，盘后，有可提，需验证合约累计盈亏=利润  无同步市值接口
    public void testContracts_leverCapital_flag1() {
        //todo:市值没有更新，无利润
        //合约追加非杠杆
        func.trade_Capital(wf.getId(),wf.getAccountId(),1000);
        //添加利润
        //改造合约盈利100
        T_cntrService t_cntrService = new T_cntrServiceImpl();
        long profit = 100;
        Long cntrId = wf.getTradeId();
        t_cntrService.updateProfit(profit,cntrId);
//        Action.sleep(30000);
        WfcurrpercentService wfcurrpercentService = new wfcurrpercentServiceImpl();
        Long tradeId = wf.getId();
        wfcurrpercentService.updatewfcurrpercent(profit,tradeId);
//        Action.sleep(30000);
        //放大
        HashMap<String, Object> map = new HashMap<>();
        float capitalAmount = 3000f;   //放大3000
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
//        lever.then().body("capitalOrder.unLeverSubAmount", equalTo(200));  //todo
//        lever.then().body("capitalOrder.profitAmount", equalTo(profit));  //todo
        lever.then().body("capitalOrder.orderType", equalTo(11003));
        lever.then().body("capitalOrder.status", equalTo(1));
    }
}
