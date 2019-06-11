package com.niu.cntr.cntrsys;

import com.niu.cntr.Api;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.core.IsEqual.equalTo;

public class Contact extends Api {
    String random=String.valueOf(System.currentTimeMillis());

    @Override
    public RequestSpecification getDefaultRequestSpecification(){
        RequestSpecification requestSpecification = super.getDefaultRequestSpecification();
        requestSpecification.contentType(ContentType.JSON);

        requestSpecification.filter( (req, res, ctx)->{
            //todo: 对请求 响应做封装
            return ctx.next(req, res);
        });
        return requestSpecification;
    }

    public ResponseSpecification getResponseSpec(){
        ResponseSpecBuilder builder = new ResponseSpecBuilder();
        builder.expectStatusCode(200);
        builder.expectBody("success",equalTo(true));
        ResponseSpecification responseSpec = builder.build();
        return responseSpec;
    }
}
