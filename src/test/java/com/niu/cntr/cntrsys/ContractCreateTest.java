package com.niu.cntr.cntrsys;

import com.niu.cntr.Cntr;
import com.niu.cntr.CntrConfig;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

//新增合约测试用例

/**
 *
 * 1、冒烟用例：
 * 2、
 */
public class ContractCreateTest {
    Product product;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(product==null){
            product=new Product();
        }
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(), TradeVO.getInstance().getAccountId());
    }


    @Test(groups = "smoke")
    //新增按天合约
    //todo：dataprovider
    public void testContract_create() {
        String productid = "52825118558251";
        JsonPath js = new JsonPath(product.queryone(productid, CntrConfig.getInstance().brandId).asString());
        List<Long> datavers = js.getList("product.datVer");
        String dataver = String.valueOf(datavers.get(0));
        List<String> cycleOptions = js.getList("product.cycleOptions");
        String initiDuration = cycleOptions.get(0).split(",")[0];
        HashMap<String, Object> map = new HashMap<>();
        map.put("id",Action.random());
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("datVer",dataver);
        map.put("productId",productid);
        map.put("initiDuration",initiDuration);
        map.put("accountId",Action.random());
        map.put("phoneNumber",Action.getTelephone());
        Response re = product.contract_create(map);
        TradeVO.getInstance().setTradeId(re.path("trade.id"));
        TradeVO.getInstance().setAccountId(re.path("trade.accountId"));
        re.then().body("success", equalTo(true));
        re.then().body("trade.status", equalTo(1));
        re.then().body("trade.wfPercent", equalTo(3300));

    }
}