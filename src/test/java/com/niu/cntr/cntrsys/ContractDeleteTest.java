package com.niu.cntr.cntrsys;

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
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.*;

//终止合约
public class ContractDeleteTest {
    Trade trade;
    String random=String.valueOf(System.currentTimeMillis());
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade==null){
            trade=new Trade();
        }
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test(groups = "smoke")
    public void testContracts_delete() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        String productId = "52825118558251";
        Response re = func.trade_new(productId,5000,10,0);
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        map.put("id", Action.random());
        map.put("accountId",accountId);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("tradeId",tradeId);
        Response delete = trade.contracts_delete(map);
        delete.then().body("success",equalTo(true));
        delete.then().body("trade.status",equalTo(3));

        JsonPath js = new JsonPath(delete.asString());
        List<Long> changedAmount = js.getList("trade.wftransactionChangeRecordList.changedAmount");
        String cd = String.valueOf(changedAmount.get(0));
        assertEquals(cd,"0.0");
        List<Long> types = js.getList("trade.wftransactionChangeRecordList.type");
        String type = String.valueOf(types.get(0));
        assertEquals(type,"11502");
    }
}