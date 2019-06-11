package com.niu.cntr.cntrsys;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;

import static io.restassured.parsing.Parser.JSON;

public class Trade extends Contact {
    //1、股票划转
    //2、合约流水查询

    //3、放大杠杆预计算
    public Response contracts_cal_leverCapital(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/contracts_cal_leverCapital.yaml",map);
    }

    //4、放大杠杆
    public Response contracts_leverCapital(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_leverCapital.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_leverCapital.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }

    //5、缩小杠杆预计算
    public Response contracts_cal_reduceCapital(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/contracts_cal_reduceCapital.yaml",map);
    }

    //6、缩小杠杆
    public Response contracts_reduceCapital(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_reduceCapital.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_reduceCapital.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }

    //7、追加非杠杆保证金
    public Response contracts_capital(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_capital.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_capital.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }

    //8、提取现金
    public Response contracts_fetchCash(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_fetchCash.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_fetchCash.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }

    //9、合约展期预计算现金
    public Response contracts_cal_renew_paidCash(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/contracts_cal_renew_paidCash.yaml",map);
    }

    //10、合约展期预计算天数
    public Response contracts_cal_renew_paidDay(HashMap<String, Object> map){
        return getResponseFromYaml("/CntrApi/contracts_cal_renew_paidDay.yaml",map);
    }

    //11、延期卖出
    public Response contracts_renew(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_renew.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_renew.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }

    //12、停牌转合约预计算
    //13、停牌转合约
    public Response contracts_convert(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_convert.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_convert.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().post(url)
                .then().log().all().extract().response();
    }
    //14、终止合约
    public Response contracts_delete(HashMap<String, Object> map){
        RestAssured.registerParser("text/plain", JSON);
        //模板映射
        String body =template("/data/contracts_delete.json",map);
        //更新url，替换url中的参数
        String url = updateUrlparam(getApiFromYaml("/CntrApi/contracts_delete.yaml").url,map);
        return getDefaultRequestSpecification().body(body)
                .when().delete(url)
                .then().log().all().extract().response();
    }

    //15、查询合约详情
    public Response contracts_queryContractDetail(Long brandId, Long memberId,Long tradeId ){
        HashMap<String,Object> map=new HashMap<>();
        map.put("brandId",brandId);
        map.put("memberId",memberId);
        map.put("tradeId",tradeId);
        return getResponseFromYaml("/CntrApi/contracts_queryContractDetail.yaml",map);
    }

}
