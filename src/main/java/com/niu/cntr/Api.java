package com.niu.cntr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static io.restassured.parsing.Parser.*;

/**
 * Created by admin on 2019/4/28.
 */
public class Api {
    HashMap<String, Object> query = new HashMap<>();
    private static Logger logger = Logger.getLogger(Api.class);
//    RestAssured.registerParser("text/plain", JSON);

    public Api() {
        useRelaxedHTTPSValidation();
    }

    public RequestSpecification getDefaultRequestSpecification() {
        return given().contentType("application/json").log().all();
    }

    public static String template(String path, HashMap<String, Object> map) {
        DocumentContext documentContext = JsonPath.parse(Api.class
                .getResourceAsStream(path));
        map.entrySet().forEach(entry -> {
            documentContext.set(entry.getKey(), entry.getValue());
        });
        return documentContext.jsonString();
    }

    public static String templateFromMustache(String path, HashMap<String, Object> map) {
        //new DefaultMustacheFactory().compile("/data/create.mustache").execute()

        return null;
        //return documentContext.jsonString();
    }

    public static String templateFromFreeMarker(String path, HashMap<String, Object> map) {
        //new DefaultMustacheFactory().compile("/data/create.mustache").execute()

        return null;
        //return documentContext.jsonString();
    }

    public Response templateFromSwagger(String path, String pattern, HashMap<String, Object> map) {
        //支持从swagger自动生成接口定义并发送
        DocumentContext documentContext = JsonPath.parse(Api.class.getResourceAsStream(path));
        map.entrySet().forEach(entry -> {
            documentContext.set(entry.getKey(), entry.getValue());
        });

        String method = documentContext.read("method");
        String url = documentContext.read("url");
        return getDefaultRequestSpecification().when().request(method, url);
    }


    public Restful getApiFromYaml(String path) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(CntrConfig.class.getResourceAsStream(path), Restful.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Restful updateApiFromMap(Restful restful, HashMap<String, Object> map) {
        if (map == null) {
            return restful;
        }
        //判断url中是否有需要替换的map数据
        if (restful.url.contains("{")) {
            String substr = restful.url.substring(restful.url.indexOf("{") + 1, restful.url.indexOf("}"));
            for (String key : map.keySet()) {
                if (key.equals(substr)) {
                    restful.url = restful.url.replace("{" + substr + "}", map.get(key).toString());
                }
            }
        }

//        if (restful.method.toLowerCase().contains("get")) {
//            map.entrySet().forEach(entry -> {
//                restful.query.replace(entry.getKey(), entry.getValue().toString());
//                System.out.println(restful.query);
//            });
//        }

        //post类型的接口带参数，查询类的接口为post类型
        map.entrySet().forEach(entry -> {
            restful.query.replace(entry.getKey(), entry.getValue().toString());

        });
//        logger.info(restful.query);

        if (restful.method.toLowerCase().contains("post")) {
            if (map.containsKey("_body")) {
                restful.body = map.get("_body").toString();
            }
            if (map.containsKey("_file")) {
                String filePath = map.get("_file").toString();
                map.remove("_file");
                restful.body = template(filePath, map);
            }
        }
        return restful;

    }

    public Response getResponseFromRestful(Restful restful) {

        RequestSpecification requestSpecification = getDefaultRequestSpecification();

        if (restful.query != null) {
            restful.query.entrySet().forEach(entry -> {
                requestSpecification.queryParam(entry.getKey(), entry.getValue());
            });
        }

        if (restful.body != null) {
            requestSpecification.body(restful.body);
        }
        String[] url = updateUrl(restful.url);

        return requestSpecification.log().all()
                .when().request(restful.method, restful.url)
                .then().log().all()
                .extract().response();


    }

    public Response getResponseFromYaml(String path, HashMap<String, Object> map) {
        //fixed: 根据yaml生成接口定义并发送
        Restful restful = getApiFromYaml(path);
        restful = updateApiFromMap(restful, map);

        //发送请求前明确使用json解析器
        RestAssured.registerParser("text/plain", JSON);
        RequestSpecification requestSpecification = getDefaultRequestSpecification();

        if (restful.query != null) {
            restful.query.entrySet().forEach(entry -> {
                requestSpecification.queryParam(entry.getKey(), entry.getValue());
            });
        }

        String[] url = updateUrl(restful.url);
        logger.info(url[1]);

        if (restful.body != null) {
            requestSpecification.body(restful.body);
            logger.info(restful.body);
        }

        return requestSpecification
                .header("Host", url[0])
                .when().request(restful.method, url[1])
                .then().log().ifError()
                .extract().response();
    }

    private String[] updateUrl(String url) {
        //fixed: 多环境支持，替换url，更新host的header

        HashMap<String, String> hosts = CntrConfig.getInstance().env.get(CntrConfig.getInstance().current);

        String host = "";
        String urlNew = "";
        for (Map.Entry<String, String> entry : hosts.entrySet()) {
            if (url.contains(entry.getKey())) {
                host = entry.getKey();
                urlNew = url.replace(entry.getKey(), entry.getValue());
            }
        }

        return new String[]{host, urlNew};
    }

    public String updateUrlparam(String url, HashMap<String, Object> map) {
        //如果url中带参数，就用参数替换
        if (url.contains("{")) {
            String substr = url.substring(url.indexOf("{") + 1, url.indexOf("}"));
            for (String key : map.keySet()) {
                if (key.equals(substr)) {
                    url = url.replace("{" + substr + "}", map.get(key).toString());
                }
            }
        }
        return url;
    }
}
