package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.inspect.SqlConnect;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.Assert.*;

//停牌转合约
//停牌股考虑是否放在config文件中 1个或多个
public class ContractConvertTest {
    Trade trade;
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

    //停牌转合约，停牌比例合约，老合约终止
    @Test(groups = "smoke")
    public void testContracts_convert() {
        //新增合约
        SqlConnect sc = new SqlConnect();
        Product product = new Product();
        HashMap<String, Object> map = new HashMap<>();
        String old_productId = "52825118558251";  //原合约产品
        Response re = func.trade_new(old_productId,5000,10,0);
        //买入股票
        Long cntrId = re.path("trade.tradeId");
        Long tradeId = re.path("trade.id");
        Long accountId = re.path("trade.accountId");
        func.sendOrder(accountId,tradeId,"300178",200);
        //等待
        Action.sleep(2000);
        //获取停牌股
        Map.Entry<String,String> suspendStk = CntrConfig.getInstance().suspendStk.entrySet().stream().findAny().get();
        //修改持仓
        sc.update("niudb","update t_cntr_pos set Stk_Cd='"+ suspendStk.getKey()+"' and Stk_Nm = '"+suspendStk.getValue()+"' where Cntr_Id = '"+cntrId+"' and Stk_Cd = '300178' ;");
        //停牌转合约 原合约终止
        //准备停牌产品信息 datVer productId  pzMultiple
        String new_productid = "52894567092551";  //停牌杠杆产品
        JsonPath js = new JsonPath(product.queryone(new_productid, CntrConfig.getInstance().brandId).asString());
        List<Long> datavers = js.getList("product.datVer");
        String dataver = String.valueOf(datavers.get(0));
        List<String> multipleOptions = js.getList("product.multipleOptions");
        String multiple = multipleOptions.get(0).split(",")[0];
        map.put("accountId",accountId);
        map.put("brandId",CntrConfig.getInstance().brandId);
        map.put("datVer",dataver);
        map.put("id",Action.random());
        map.put("productId",new_productid);
        map.put("pzMultiple",multiple);
        map.put("stocks","002199");
        map.put("tradeId",tradeId);
        try {
            Response cn = trade.contracts_convert(map);
            cn.then().body("converTrade.tradeId", equalTo(tradeId));
            cn.then().body("converTrade.status", equalTo(1));
            cn.then().body("converTrade.oldEnd", equalTo(false));
            cn.then().body("converTrade.newTrade.status", equalTo(1));
            cn.then().body("success", equalTo(true));
            //检验原合约是否终止
            trade.contracts_queryContractDetail(CntrConfig.getInstance().brandId,accountId,tradeId).then().body("status",equalTo(3));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}