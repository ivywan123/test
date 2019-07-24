package com.niu.cntr.Service.CntrService;

import com.niu.cntr.mapper.wftransactionmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;
import org.apache.ibatis.annotations.Param;

public interface WfcurrpercentService {
    Integer updatewfcurrpercent(@Param("profit") long profit, @Param("id") long tradeId); //更新合约市值表
}
