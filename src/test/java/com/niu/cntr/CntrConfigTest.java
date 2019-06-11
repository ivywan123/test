package com.niu.cntr;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;

/**
 * Created by admin on 2019/5/6.
 */
public class CntrConfigTest {
    @BeforeTest
    public void setUp() throws Exception {

    }

    @AfterTest
    public void tearDown() throws Exception {

    }

    @Test
    public void getInstance() throws Exception {
        CntrConfig.getInstance();
    }

    @Test
    void load() {
        String path="/conf/CntrConfig.yaml";
        assertNotEquals(CntrConfig.load(path),null);
    }
}