package com.niu.cntr.redisConfig;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by xd on 2019/6/19.
 */
public final class redisUtils {
    public static RedisTemplate redis1;
    public static RedisTemplate redis2;
    static {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-redis-cntr.xml");
        redis1 = applicationContext.getBean(RedisTemplate.class);
        ApplicationContext applicationContext2 = new ClassPathXmlApplicationContext("spring-redis-trade.xml");
        redis2 = applicationContext2.getBean(RedisTemplate.class);
    }
    public static RedisTemplate getRedisConnect(DataSourceEnvironment dataSourceEnvironment){
        if (DataSourceEnvironment.cntr==dataSourceEnvironment){
            return redis1;
        }else if (DataSourceEnvironment.trade==dataSourceEnvironment){
            return redis2;
        }else
            return null;
    }

    public static enum DataSourceEnvironment {
        cntr,
        trade;
    }
}
