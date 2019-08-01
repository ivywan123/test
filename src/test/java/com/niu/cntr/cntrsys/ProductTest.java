package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by admin on 2019/4/28.
 */
@Test(groups = "open")
public class ProductTest {
    Product product;

    @BeforeMethod
    void setUp(){
        if(product==null){
            product=new Product();
        }
    }

    @Test
    void list(){
        //验证股100品牌下所有的产品总数为10
        //ResponseSpecification重用
        Long brandId = CntrConfig.getInstance().brandId;
        product.list(brandId).then()
                .spec(product.getResponseSpec());
    }

    //查询某个产品信息测试
    @DataProvider(name = "productquery")
    public Object[][] providedata(){
    return new Object[][]{{"52825118558251","52989279149851","product.productName[0]","财云-按天操盘"}};
    }

    @Feature("产品查询")
    @Description("产品查询-冒烟用例")
    @Test(dataProvider = "productquery")
    void queryone(String productId,String brandId,String expt,String exptvalue){
        product.queryone(productId,Long.parseLong(brandId)).then().spec(product.getResponseSpec())
                .body(expt,equalTo(exptvalue));
    }

}
