package com.niu.cntr.Service.TradeDaoImpl;

import com.niu.cntr.Service.TradeService.T_cntrService;
import com.niu.cntr.mapper.t_cntrmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;

import java.math.BigDecimal;

public class T_cntrServiceImpl implements T_cntrService {
    @Override
    public Integer updateProfit(long profit, long cntr_id) {
        t_cntrmapper mapper = MapperFactory.createMapper(t_cntrmapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.trade);
        Integer result  = mapper.updateProfit(profit,cntr_id);
        return result;
    }
}
