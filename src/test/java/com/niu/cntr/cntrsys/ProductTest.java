package com.niu.cntr.cntrsys;

import com.niu.cntr.CntrConfig;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by admin on 2019/4/28.
 */
public class ProductTest {
    Product product;

    @BeforeTest
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
                .spec(product.getResponseSpec())
                .body("totalCount",equalTo(10));
    }

    //查询所有产品时不传品牌ID
    @Test
    void listwhinnull(){
        product.list(null).then().statusCode(200).body("errorCode",equalTo(001));
    }


    //查询某个产品信息测试
    @DataProvider(name = "productquery")
    public Object[][] providedata(){
    return new Object[][]{{"52825118558251","52989279149851","product.productName[0]","财云-按天操盘"}};
    }

    @Test(dataProvider = "productquery")
    void queryone(String productId,Long brandId,String expt,String exptvalue){
        product.queryone(productId,brandId).then().spec(product.getResponseSpec())
                .body(expt,equalTo(exptvalue));
    }

}
