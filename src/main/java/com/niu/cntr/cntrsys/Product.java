package com.niu.cntr.cntrsys;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;

import static io.restassured.parsing.Parser.JSON;

/**
 * Created by admin on 2019/4/28.
 */
public class Product extends Contact {

    //1、查询某品牌下的所有产品
    public Response list(Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("brandId",brandId);
        return getResponseFromYaml("/CntrApi/product_list.yaml",map);
    }

    //2、根据产品id和品牌id，查询某产品信息
    public Response queryone(String productId,Long brandId){
        HashMap<String,Object> map=new HashMap<>();
        map.put("id",productId);
        map.put("brandId",brandId);
        return getResponseFromYaml("/CntrApi/products_id.yaml",map);
    }

    //3、预计算合约
    public Response product_calculation(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/product_calculation.yaml",map);
    }

    //4、新增合约2
    public Response contract_create(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/product_contract.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/product_contract.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }
}
