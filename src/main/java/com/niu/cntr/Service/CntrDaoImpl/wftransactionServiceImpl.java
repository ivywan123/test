package com.niu.cntr.Service.CntrDaoImpl;

import com.niu.cntr.Service.CntrService.BrandService;
import com.niu.cntr.Service.CntrService.WftransactionService;
import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.mapper.wftransactionmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;

public class wftransactionServiceImpl implements WftransactionService {
    @Override
    public Integer updateEndtradedate(wftransaction wftransaction){
        wftransactionmapper mapper = MapperFactory.createMapper(wftransactionmapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.cntr);
        Integer result = mapper.updateEndtradedate(wftransaction);
        return result;
    }


}
