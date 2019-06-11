package com.niu.cntr;

import io.restassured.RestAssured;

/**
 * Created by admin on 2019/5/6.
 */
public class Cntr {
    //合约系统加密
    private String auth;

    /**
    public  String getAuth(String json){
        return RestAssured.given().log().all()
                .queryParam("json",)
                .queryParam("brandId",CntrConfig.getInstance().brandId)
                .when().get("")
                .then().log().all().statusCode(200)
                .extract().path("access_auth");
    }

    public String getAuthso(){
        if(auth==null){
            auth=getAuth();
        }
        return auth;
    }
**/

}
