package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.equalTo;

//预计算接口测试
public class ProductcalculationTest {
    Product product;

    @BeforeTest
    void setUp(){
        if(product==null){
            product=new Product();
        }
    }

    @DataProvider(name = "productcal")
    public Object[][] providedata(){
        return new Object[][]{{"52989279149851","2000","6","52825118558251"}
                            ,{"52989279149851","2000","6","53897715485425","52825118558251"}
                            ,{"52989279149851","2000","6","1","53897715485425","52825118558251"}
                            ,{"52989279149851","2000","6","2","53897715485425","52825118558251"}};
    }


    @Test
    //1、不带初始周期和用户账户，预计算一个财云按天合约，2000,6倍
    public void product_calculation() {
        HashMap<String,Object> map = new HashMap<>();
        //用产品信息接口获取数据
        map.put("brandId","52989279149851");
        map.put("borrowAmount",2000);
        map.put("pzMultiple",6);
        map.put("productId","52825118558251");
        Response re=product.product_calculation(map);
        re.then().spec(product.getResponseSpec());
        re.then().body("trade.wfPercent",equalTo(2333));
        re.then().body("trade.wfDuration",equalTo(2));
        re.then().body("trade.initiDuration",equalTo(2)); //不传初始周期数，默认取产品最小
//        re.then().body("trade.accountMgAmt",equalTo(new BigDecimal(3.96)));
//        re.then().body("trade.firstTradeDate",equalTo("2019-05-10 00:00:00"));
        re.then().body("trade.statusNm",equalTo("申请中"));

    }

    //2、带accountid的调用，用json Schema Validation 验证必要字段是否存在 product_calculation_schema.json
//    get("https://server/demo?p1=0&p2=1").then().assertThat().body(matchesJsonSchemaInClasspath("test.json"));
    @Test
    public void product_calculation_accountid(){
        HashMap<String,Object> map = new HashMap<>();
        //用产品信息接口获取数据
        map.put("brandId","52989279149851");
        map.put("borrowAmount",2000);
        map.put("pzMultiple",6);
        map.put("accountId","53897715485425");
        map.put("productId","52825118558251");
        Response re=product.product_calculation(map);
        re.then().spec(product.getResponseSpec());
        re.then().body("trade.wfPercent",equalTo(2333));
        re.then().body("trade.wfDuration",equalTo(2));
        re.then().body("trade.statusNm",equalTo("申请中"));
        re.then().assertThat().body(matchesJsonSchemaInClasspath("product_calculation_schema.json"));

    }

    //3、验证初始周期数参数，传的话，判断是否在产品列表范围中，不在报错；在的话用传的参数
    @Test
    public void product_calculation_initiDuration() {
        HashMap<String, Object> map = new HashMap<>();
        //用产品信息接口获取数据
        String productId = "52825118558251";
        String brandId = "52989279149851";
        map.put("brandId", "52989279149851");
        map.put("borrowAmount", 2000);
        map.put("pzMultiple", 6);
        map.put("accountId", "53897715485425");
        map.put("productId", "52825118558251");
        //获取产品期限选项字段cycleOptions，分别传大于最大值，小于最小值
        JsonPath js = new JsonPath(product.queryone(productId, CntrConfig.getInstance().brandId).asString());
        List<String> cycleOptions = js.getList("product.cycleOptions");
        String cycle = cycleOptions.get(0).split(",")[0];
        Integer max = Integer.parseInt(Collections.max(cycleOptions));
        Integer min = Integer.parseInt(Collections.max(cycleOptions));

        map.put("initiDuration",max+1);
        Response re=product.product_calculation(map);
        re.then().spec(product.getResponseSpec());
        re.then().body("success",equalTo(false));
        re.then().body("errCode",equalTo(211));
        re.then().body("resultMsg",equalTo("产品初始周期数无效"));

        map.put("initiDuration",min-1);
        Response re2=product.product_calculation(map);
        re2.then().spec(product.getResponseSpec());
        re2.then().body("success",equalTo(false));
        re2.then().body("errCode",equalTo(211));
        re2.then().body("resultMsg",equalTo("产品初始周期数无效"));


    }
}
