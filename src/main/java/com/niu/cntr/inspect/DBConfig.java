package com.niu.cntr.inspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.HashMap;

public class DBConfig {
    public HashMap<String, HashMap<String,String>> database;
    public String dbname = "niudb";
    public String dburl = "192.168.1.60";
    public String user = "root";
    public String pwd = "root";

    private static DBConfig dbconfig;

    public static DBConfig getInstance(){
        if(dbconfig==null){
            dbconfig=load("/conf/dbConfig.yaml");
        }
        return dbconfig;
    }

    public static DBConfig load(String path){
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try{
            return mapper.readValue(DBConfig.class.getResourceAsStream(path),DBConfig.class);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
