package com.niu.cntr.cntrsys;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.niu.cntr.CntrConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV4;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.equalTo;

//预计算接口测试
@Test(groups = "open")
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

    public void product_calculation_accountid(){
        HashMap<String,Object> map = new HashMap<>();
        //用产品信息接口获取数据
        map.put("brandId","52989279149851");
        map.put("borrowAmount",2000);
        map.put("pzMultiple",6);
        map.put("accountId","53897715485425");
        map.put("productId","52825118558251");
//        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder().setValidationConfiguration(ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV4).freeze()).freeze();
        Response re=product.product_calculation(map);
        re.then().spec(product.getResponseSpec());
        re.then().body("trade.wfPercent",equalTo(2333));
        re.then().body("trade.wfDuration",equalTo(2));
        re.then().body("trade.statusNm",equalTo("申请中"));
//        re.then().assertThat().body(matchesJsonSchemaInClasspath("/data/product_calculation_schema.json").using(jsonSchemaFactory));

    }

    //3、验证初始周期数参数，传的话，判断是否在产品列表范围中，不在报错；在的话用传的参数

    public void product_calculation_initiDuration() {
        HashMap<String, Object> map = new HashMap<>();
        //用产品信息接口获取数据
        String productId = "52825118558251";
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("borrowAmount", 2000);
        map.put("pzMultiple", 6);
        map.put("accountId", "53897715485425");
        map.put("productId", "52825118558251");
        //获取产品期限选项字段cycleOptions，分别传大于最大值，小于最小值，最小值
        JsonPath js = new JsonPath(product.queryone(productId, CntrConfig.getInstance().brandId).asString());
        List<String> cycleOptions = js.getList("product.cycleOptions");
        String str[] = cycleOptions.get(0).split(",");
        /**拿到的min和max不对
         List<String> cycle = Arrays.asList(str);
        String max = Collections.max(cycle);
        String min = Collections.max(cycle);
        **/
        Integer a = Integer.parseInt(str[str.length-1])+1;  //仅在产品使用期限选项是升序编辑时有效
        Integer b = Integer.parseInt(str[0])-1;
        map.put("initiDuration",a);
        Response re=product.product_calculation(map);
//        re.then().spec(product.getResponseSpec());  //反例不要使用
        re.then().body("success",equalTo(false));
        re.then().body("errCode",equalTo("211"));
        re.then().body("resultMsg",equalTo("产品初始周期数无效"));

        map.put("initiDuration",b);
        Response re2=product.product_calculation(map);
        System.out.println(re2.getBody());
//        re2.then().spec(product.getResponseSpec());  //反例不要使用
        re2.then().body("success",equalTo(false));
        re2.then().body("errCode",equalTo("211"));
        re2.then().body("resultMsg",equalTo("产品初始周期数无效"));

        map.put("initiDuration",Integer.parseInt(str[0]));
        Response re3=product.product_calculation(map);
        re3.then().spec(product.getResponseSpec());
        re3.then().body("trade.wfPercent",equalTo(2333));
        re3.then().body("trade.wfDuration",equalTo(2));
        re3.then().body("trade.initiDuration",equalTo(Integer.parseInt(str[0])));
        re3.then().body("trade.statusNm",equalTo("申请中"));

    }
}
