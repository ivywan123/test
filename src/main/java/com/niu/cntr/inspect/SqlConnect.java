package com.niu.cntr.inspect;

import net.minidev.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SqlConnect {

    //sqlconnect
    /**
     * 获取数据库连接实例
     *
     */
    public Connection getconnect(String databaseName) throws Exception{
        HashMap<String,String> db = DBConfig.getInstance().database.get(databaseName);
        Connection conn = null;
        String url = "jdbc:mysql://"+db.get("url")+":3306/"+databaseName+"?autoReconnect=true";
        String user = db.get("user");
        String pwd = db.get("pwd");
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pwd);
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 查询
     * @param sql
     */
    public  String Select(String databaseName,String sql) {
        List<String> result = new ArrayList<String>();
        Connection conn2 = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn2 = getconnect(databaseName);
            st = conn2.createStatement();
            rs = st.executeQuery(sql);
            ResultSetMetaData rm = rs.getMetaData();
            while(rs.next()) {
                JSONObject json_obj = new JSONObject();
                for (int i=1; i<=rm.getColumnCount(); i++){
                    json_obj.put(rm.getColumnLabel(i), rs.getString(i));
                }
                result.add(json_obj.toString());
            }
            //分别捕获异常
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            close(conn2, st, rs);
        }
        return String.join(",",result);
    }

    /**
     * 插入修改 删除
     * @param sql
     */
    public  String update (String databaseName,String sql) {
        System.out.println( sql);
        List<String> result = new ArrayList<String>();
        Connection conn3 = null;
        Statement st = null;
        int res=0;  //返回更新的记录条数，如果返回为0，表示未更新
        try {
            conn3 = getconnect(databaseName);
            st = conn3.createStatement();
            //注意，此处是excuteUpdate()方法执行
            res=st.executeUpdate(sql);
            //分别捕获异常
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(st != null) {
                    st.close();
                    st = null;
                }
                if(conn3 != null) {
                    conn3.close();
                    conn3 = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        JSONObject json_obj = new JSONObject();
        json_obj.put("cnt", String.valueOf(res));
        result.add(json_obj.toString());
        return String.join(",",result);
    }

    /**
     * 关闭数据库连接
     */
    private void close(Connection con,Statement sta,ResultSet rs){

        try {
            if(rs !=null)rs.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            log.error(e.getMessage());
        }

        try {
            if(sta !=null)sta.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            log.error(e.getMessage());
        }

        try {
            if(con !=null)con.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            log.error(e.getMessage());
        }
    }

}
