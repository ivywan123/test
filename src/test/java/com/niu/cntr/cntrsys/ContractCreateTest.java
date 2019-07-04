package com.niu.cntr.cntrsys;

import com.niu.cntr.Cntr;
import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
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
    wftransaction wf;

    @BeforeMethod
    public void setUp() {
        if(product==null){
            product=new Product();
        }
        if(wf == null){
            wf = new wftransaction();
        }
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(wf.getId(), wf.getAccountId());
    }


    @Test(groups = "open")
    //新增按天合约
    //todo：dataprovider
    public void testContract_create() {
        String productid = "52825118558251";
        BigDecimal borrowAmount = new BigDecimal(3000);
        BigDecimal pzMultiple = new BigDecimal(10);
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
        map.put("borrowAmount",borrowAmount);
        map.put("pzMultiple",pzMultiple);
        //断言数据
        BigDecimal leverCapitalAmount = borrowAmount.divide(pzMultiple,0, BigDecimal.ROUND_HALF_UP);
        BigDecimal wfPercent = borrowAmount.add(leverCapitalAmount);
        Response re = product.contract_create(map);
        wf.setId(re.path("trade.id"));
        wf.setAccountId(re.path("trade.accountId"));
        re.then().body("success", equalTo(true));
        re.then().body("trade.status", equalTo(1));
        re.then().body("trade.wfPercent", equalTo(wfPercent));

    }
}