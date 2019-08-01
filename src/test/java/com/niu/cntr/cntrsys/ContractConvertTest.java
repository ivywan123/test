package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.Service.TradeDaoImpl.T_cntr_posServiceImpl;
import com.niu.cntr.Service.TradeService.T_cntr_posService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

//停牌转合约
//停牌股考虑是否放在config文件中 1个或多个
//1、合约无停牌股，原合约保留
//2、合约有停牌股和非停牌股，传入停牌股
//3、合约只有多支停牌股，传入部分停牌股
//4、合约只有多支停牌股，传入全部停牌股（normal） 原合约终止和保留 true

@Test(groups = "open")
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

    //1、合约无停牌股
    @Feature("停牌转合约")
    @Description("停牌转合约-合约无停牌股")
    public void testContracts_convert_nostk(){
        Product product = new Product();
        HashMap<String, Object> map = new HashMap<>();
        //买入一只股票
        String stk_cd1 ="000001";
        Integer qty = 200;
        Response re =func.sendOrder(wf.getAccountId(),wf.getId(),stk_cd1,qty);
        //盘后生成的合约，不能马上买入
        if(re.path("success").equals(false)){
            return;
        }
        //等待
        Action.sleep(2000);
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
        map.put("stocks","000001");
        map.put("tradeId",wf.getId());
        try {
            Response cn = trade.contracts_convert(map);
            cn.then().body("success", equalTo(false));
            cn.then().body("errCode", equalTo("001"));
            cn.then().body("status", equalTo("false"));
            cn.then().body("resultMsg", equalTo("停牌股转合约终止旧合约,必须转移全部持仓"));
            //未执行停牌转合约，原合约不终止
            func.queryTrade(wf.getBrandId(),wf.getAccountId(),wf.getId()).then().body("trade.status",equalTo(1));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DataProvider(name = "retain")
    public Object[][] retain(){
        return new Object[][]{{false}
                ,{true}};
    }

    //4、合约有多支停牌股，传入全部停牌股（normal） 原合约终止和保留
    //盘中
    @Feature("停牌转合约")
    @Description("停牌转合约-合约有多支停牌股，传入全部停牌股（normal） 原合约终止和保留")
    @Test(dataProvider="retain")
    public void testContracts_convert(boolean retain) {
        Product product = new Product();
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<String> stks = new ArrayList<>();
        Integer qty = 200;
        stks.add("000001");
        stks.add("600123");
        //获取停牌股
        T_cntr_posService t_cntr_posService = new T_cntr_posServiceImpl();
        String suspendStk = CntrConfig.getInstance().suspendStk;
        String str[] = suspendStk.split(",");
        int num;
        ArrayList<String> suspendstk_use = new ArrayList<>();
        for(int i=0;i<stks.size();i++){
            Response re =func.sendOrder(wf.getAccountId(),wf.getId(),stks.get(i),qty);
            //盘后生成的合约，不能马上买入，直接返回，会断言用例为true
            if(re.path("success").equals(false)){
                return;
            }
            //等待
            Action.sleep(20000);
            String stkNm = func.queryStkNm(str[i], wf.getBrandId()).path("stock.stockName");
            //修改合约持仓
            num=t_cntr_posService.updatePos(str[i], stkNm, wf.getTradeId(), stks.get(i));
            if(num>0) {
                suspendstk_use.add(str[i]);
            }
        }
        String Stkstr = org.apache.commons.lang3.StringUtils.join(suspendstk_use.toArray(),",");

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
        map.put("retain",retain);  //从dataprovider获取的参数
        try {
            Response cn = trade.contracts_convert(map);
            cn.then().body("success", equalTo(true));
            cn.then().body("converTrade.tradeId", equalTo(wf.getId()));  //验证老合约编号
            cn.then().body("converTrade.status", equalTo(1));
            cn.then().body("converTrade.newTrade.status", equalTo(1));
            int status = trade.contracts_queryContractDetail(wf.getBrandId(), wf.getAccountId(), wf.getId()).then().extract().path("trade.status");
            if(retain == false) {
                //检验原合约是否保留
                Assert.assertEquals(status,1);
                cn.then().body("converTrade.oldEnd", equalTo(false));
            }else {
                //检验原合约是否终止
                Assert.assertEquals(status,3);
                cn.then().body("converTrade.oldEnd", equalTo(true));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}