package com.niu.cntr.inspect;


import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Action {

    //返回固定长度的随机数，前面补0
    public static String getFixLenthString(int strLength) {
        Random rm = new Random();
        // 获得随机数
        double pross = (1 + rm.nextDouble()) * Math.pow(10, strLength);
        // 将获得的获得随机数转化为字符串
        String fixLenthString = String.valueOf(pross);
        // 返回固定的长度的随机数
        return fixLenthString.substring(1, strLength + 1);
    }

    public void updateJson(String path,String key,String value){

    }

    public static   String  JsonPath(String json,String expression ) throws IOException {
        String GetValue=new String();

        try{
            if(json!=null&&expression!=null) {
                GetValue= JsonPath.read(json, expression).toString();
                if(GetValue.contains("[")){
                    GetValue=GetValue.substring(GetValue.indexOf("[")+1,GetValue.length()-1);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("JsonPath取值失败：");
        }

        return GetValue.replaceAll("\"","");
    }

    public static String random(){
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取随机手机号码，默认使用138号段
     */
    public static String getTelephone(){
        String[] telFirst ="134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
        int index =  getNum(0,telFirst.length-1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1,888)+10000).substring(1);
        String third = String.valueOf(getNum(1,9100)+10000).substring(1);
        return first+second+third;
    }

    /**
     *
     * @param start
     * @param end
     * @return
     */
    public static int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }

    /**
     * 睡眠等待传参
     * @return
     */
    public  static String sleep(Integer num) {
        try {
            Thread.sleep(num);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  Message.Wait;
    }

    /**
     * 获取当前时间
     * 返回 yyyy:MM:dd:00:00:00
     * @param
     * @return
     */
    public static String Time() {
        SimpleDateFormat df	= new SimpleDateFormat( "yyyy-MM-dd 00:00:00" );
        return df.format(new Date()); //获取系统当前时间
    }

    /**
     7    * 获取现在时间
     8    *
     9    * @return 返回时间类型 yyyy-MM-dd 00:00:00
     10    */
    public static Date getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }


    /**
     * 将字符串转换成map
     * @param
     * @return
     */
    public static HashMap<String,Object> strTomap(String str){
        Gson gson = new Gson();
        HashMap<String,Object> map = new HashMap<>();
        map = gson.fromJson(str,map.getClass());
        return map;
    }

    /**
    *返回停牌股

     */
//    public static
}


