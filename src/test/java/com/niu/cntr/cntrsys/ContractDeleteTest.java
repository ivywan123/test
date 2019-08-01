package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
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
//1、正常终止
//2、分润的终止，补偿收入
//3、
@Test(groups = "open")
public class ContractDeleteTest {
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
    }

    @Feature("删除合约")
    @Description("删除合约-冒烟用例")
    public void testContracts_delete() {
        //新增合约
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", Action.random());
        map.put("accountId",wf.getAccountId());
        map.put("brandId", wf.getBrandId());
        map.put("tradeId",wf.getId());
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