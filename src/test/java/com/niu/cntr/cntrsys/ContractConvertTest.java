package com.niu.cntr.cntrsys;

import com.niu.cntr.Cntr;
import com.niu.cntr.CntrConfig;
import com.niu.cntr.Service.TradeDaoImpl.T_cntr_posServiceImpl;
import com.niu.cntr.Service.TradeService.T_cntr_posService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.inspect.SqlConnect;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.Assert.*;

//停牌转合约
//停牌股考虑是否放在config文件中 1个或多个
//1、合约无停牌股
//2、合约有通排骨和非停牌股，传入全部停牌股
//3、合约有多支停牌股，传入部分停牌股
//4、合约有多支停牌股，传入全部停牌股（normal） 原合约终止
//5、合约有多支停牌股，传入全部停牌股（normal） 原合约保留

public class ContractConvertTest {
    Trade trade;
    Func func = new Func();
    wftransaction wf;

    @BeforeMethod
    public void setUp() {
        if(wf == null){
            wf = new wftransaction();
        }
        if(trade==null){
            trade=new Trade();
        }

        //准备一个无股票的待测合约，财云按天
        String productId = "52825118558251";  //原合约产品
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

    //4、合约有多支停牌股，传入全部停牌股（normal） 原合约终止
    //盘中
    @Test(groups = "open")
    public void testContracts_convert() {
        Product product = new Product();
        HashMap<String, Object> map = new HashMap<>();
        //买入两只股票
        String stk_cd1 ="000001";
        String stk_cd2 = "600123";
        Integer qty = 200;
        Response re =func.sendOrder(wf.getAccountId(),wf.getId(),stk_cd1,qty);
        //盘后生成的合约，不能马上买入
        if(re.path("status").equals("false")){
            return;
        }
        func.sendOrder(wf.getAccountId(),wf.getId(),stk_cd2,qty);
        //等待
        Action.sleep(2000);
        //获取停牌股
        T_cntr_posService t_cntr_posService = new T_cntr_posServiceImpl();
        String suspendStk = CntrConfig.getInstance().suspendStk;
        String str[] = suspendStk.split(",");
        String Stkstr = "";
        if(suspendStk.length() != 0) {
            for(int i=0;i<2;i++){
                String stk = str[i];
                System.out.println("stk_cd"+i);
                String stkNm = func.queryStkNm(stk,wf.getBrandId()).path("stock.stockName");
                //修改合约持仓
                t_cntr_posService.updatePos(stk, stkNm, wf.getTradeId(), "stk_cd"+i);
            }
            Stkstr = str[0]+","+str[1];
        }
        //获取合约停牌股

        //停牌转合约 原合约终止
        //准备停牌产品信息 datVer productId  pzMultiple
        String new_productid = "52894567092551";  //停牌杠杆产品
        JsonPath js = new JsonPath(product.queryone(new_productid, wf.getBrandId()).asString());
        List<Long> datavers = js.getList("product.datVer");
        String dataver = String.valueOf(datavers.get(0));
        List<String> multipleOptions = js.getList("product.multipleOptions");
        String multiple = multipleOptions.get(0).split(",")[0];
        map.put("accountId",wf.getAccountId());
        map.put("brandId",wf.getBrandId());
        map.put("datVer",dataver);
        map.put("id",Action.random());
        map.put("productId",new_productid);
        map.put("pzMultiple",multiple);
        map.put("stocks",Stkstr);
        map.put("tradeId",wf.getId());
        try {
            Response cn = trade.contracts_convert(map);
            cn.then().body("converTrade.tradeId", equalTo(wf.getId()));
            cn.then().body("converTrade.status", equalTo(1));
            cn.then().body("converTrade.oldEnd", equalTo(false));
            cn.then().body("converTrade.newTrade.status", equalTo(1));
            cn.then().body("success", equalTo(true));
            //检验原合约是否终止
            trade.contracts_queryContractDetail(wf.getBrandId(),wf.getAccountId(),wf.getId()).then().body("status",equalTo(3));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}