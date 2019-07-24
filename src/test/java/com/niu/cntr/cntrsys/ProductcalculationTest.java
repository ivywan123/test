package com.niu.cntr.cntrsys;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.niu.cntr.CntrConfig;
import com.niu.cntr.inspect.Action;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import static org.hamcrest.Matchers.equalTo;

//新增合约预计算接口测试
//不需要传的参数，一律传0
@Test(groups = "open")
public class ProductcalculationTest {
    Product product;

    @BeforeMethod
    void setUp(){
        if(product==null){
            product=new Product();
        }
    }

    @DataProvider(name = "productcal")
    public Object[][] providedata(){
        return new Object[][]{
                            {"52825118558251",CntrConfig.getInstance().brandId,2000L,6,0,0L}
                            ,{"52825118558251",CntrConfig.getInstance().brandId,2000L,6,0,Long.parseLong(Action.random())}
                            ,{"52825118558251",CntrConfig.getInstance().brandId,2000L,6,2,Long.parseLong(Action.random())}};
    }

    //1、不带初始周期和用户账户，预计算一个财云按天合约，2000,6倍
    @Test(dataProvider = "productcal")
    public void product_calculation(String productId,Long brandId,Long borrowAmount,int pzMultiple,int initiDuration,Long accountId) {
        HashMap<String,Object> map = new HashMap<>();
        //用产品信息接口获取数据
        map.put("brandId",brandId);
        map.put("borrowAmount",borrowAmount);
        map.put("pzMultiple",pzMultiple);
        map.put("productId",productId);
        map.put("initiDuration",initiDuration);
        map.put("accountId",accountId);
        Response re=product.product_calculation(map);
        re.then().spec(product.getResponseSpec());
        re.then().body("trade.wfPercent",equalTo(2333));
        re.then().body("trade.wfDuration",equalTo(2));
        re.then().body("trade.initiDuration",equalTo(2)); //不传初始周期数，默认取产品最小
//        re.then().body("trade.accountId",equalTo(accountId));
        re.then().body("trade.statusNm",equalTo("申请中"));
    }

    @DataProvider(name = "initiDuration")
    public Object[][] getinit(){
        List<Integer> initList = new ArrayList<>();
        Product product=new Product();
        String productId = "52825118558251";
        Long brandId = CntrConfig.getInstance().brandId;
        //获取产品期限选项字段cycleOptions，分别传大于最大值，小于最小值，最小值
        JsonPath js = new JsonPath(product.queryone(productId, brandId).asString());
        List<String> cycleOptions = js.getList("product.cycleOptions");
        String str[] = cycleOptions.get(0).split(",");
        Integer max = Integer.parseInt(str[str.length-1])+1;  //仅在产品使用期限选项是升序编辑时有效，比最大值还大
        Integer min = Integer.parseInt(str[0])-1;   //比最小值还小
        return new Object[][]{
                {productId,brandId,max},
                {productId,brandId,min}
        };
    }

    @Test(dataProvider = "initiDuration")
    //3、验证初始周期数参数，传的话，判断是否在产品列表范围中，不在报错；在的话用传的参数
    public void product_calculation_initiDuration(String productId,Long brandId,Integer init) {
        HashMap<String, Object> map = new HashMap<>();
        //用产品信息接口获取数据
        map.put("brandId", brandId);
        map.put("borrowAmount", 2000);
        map.put("pzMultiple", 6);
        map.put("accountId", Action.random());
        map.put("productId", productId);
        map.put("initiDuration",init);
        Response re=product.product_calculation(map);
//        re.then().spec(product.getResponseSpec());  //反例不要使用
        re.then().body("success",equalTo(false));
        re.then().body("errCode",equalTo("211"));
        re.then().body("resultMsg",equalTo("产品初始周期数无效"));
    }
}
