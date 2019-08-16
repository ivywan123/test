package com.niu.cntr.cntrsys;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.log4j.Logger;

import java.util.HashMap;

import static io.restassured.parsing.Parser.JSON;

/**
 * Created by admin on 2019/4/28.
 */
public class Product extends Contact {
    private static Logger logger = Logger.getLogger(Product.class);

    //1、查询某品牌下的所有产品
    public Response list(Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        if(brandId == null){
            brandId = 0L;
        }
        map.put("brandId",brandId);
        Response re = getResponseFromYaml("/CntrApi/product_list.yaml",map);
        logger.info(map.toString());
        logger.info(re.body().asString());
        return re;
    }

    //2、根据产品id和品牌id，查询某产品信息
    public Response queryone(String productId,Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("id",productId);
        map.put("brandId",brandId);
        Response re = getResponseFromYaml("/CntrApi/products_id.yaml",map);
        logger.info(map.toString());
        logger.info(re.body().asString());
        return re;
    }

    //3、预计算合约
    public Response product_calculation(HashMap<String, Object> map){
        Response re = getResponseFromYaml("/CntrApi/product_calculation.yaml",map);
        logger.info(map.toString());
        logger.info(re.body().asString());
        return re;
    }

    //4、新增合约2
    public Response contract_create(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/product_contract.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/product_contract.yaml").url,map);
        logger.info(url);
        logger.info(map.toString());
        Response re = getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
        logger.info(re.body().asString());
        return re;
    }
}
