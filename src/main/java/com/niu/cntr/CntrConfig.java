package com.niu.cntr;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by admin on 2019/4/28.
 */
public class CntrConfig {
    public HashMap<String, HashMap<String,String>> env;
    public String current = "test";
    public Long brandId = 52989279149851L;
    public LinkedHashMap<String,String> suspendStk;
    private static CntrConfig cntrConfig;

    /**
     * 单例设计模式：保证一个类只有一个实例对象，并且提供一个访问该实例的全局访问点
     * 懒汉式
     */
    public static CntrConfig getInstance(){
        if(cntrConfig==null){
            cntrConfig=load("/conf/CntrConfig.yaml");
            System.out.println(cntrConfig);
            System.out.println(cntrConfig.current);
            System.out.println(cntrConfig.suspendStk);
//            Map.Entry<String,String> item = cntrConfig.suspendStk.entrySet().stream().findFirst().orElse(null);
            //orelse 里面的null可以换成一个默认值，避免null异常，确定不为空可以直接用get方法
            Map.Entry<String,String> item = cntrConfig.suspendStk.entrySet().stream().findAny().orElse(null);
//            System.out.println(item);
        }
        return cntrConfig;
    }

    public static CntrConfig load(String path){
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try{
            return mapper.readValue(CntrConfig.class.getResourceAsStream(path),CntrConfig.class);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
