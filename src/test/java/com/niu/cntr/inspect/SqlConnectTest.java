package com.niu.cntr.inspect;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;

import static org.testng.Assert.*;

public class SqlConnectTest {
    DBConfig dbconfig;
    SqlConnect sql;
    @BeforeMethod
    public void setUp() {
        if(sql == null){
            sql = new SqlConnect();
        }
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testGetconnect() throws Exception {
        Connection a = sql.getconnect("niudb");
        assertNotNull(a);
    }

    @Test
    public void testSelect() {
    }

    @Test
    public void testUpdate() {
    }
}