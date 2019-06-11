package com.niu.cntr;

import java.util.HashMap;

/**
 * Created by admin on 2019/5/6.
 */
public class Restful {
    public String url;
    public String method;
    public HashMap<String, String> headers;
    public HashMap<String, String> query=new HashMap<String, String>();
    public String body;
}
