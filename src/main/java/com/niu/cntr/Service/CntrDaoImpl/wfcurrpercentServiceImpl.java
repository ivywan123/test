package com.niu.cntr.Service.CntrDaoImpl;

import com.niu.cntr.Service.CntrService.WfcurrpercentService;
import com.niu.cntr.mapper.wfcurrpercentmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;
import org.apache.ibatis.annotations.Param;

public class wfcurrpercentServiceImpl implements WfcurrpercentService {
    @Override
    public Integer updatewfcurrpercent(long profit,long id){
        wfcurrpercentmapper mapper = MapperFactory.createMapper(wfcurrpercentmapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.cntr);
        Integer result = mapper.updatewfcurrpercent(profit,id);
        return result;
    }
}
