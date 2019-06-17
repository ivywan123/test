package com.niu.cntr.Service.TradeDaoImpl;

import com.niu.cntr.Service.TradeService.RoleService;
import com.niu.cntr.entity.brand;
import com.niu.cntr.entity.role;
import com.niu.cntr.mapper.brandmapper;
import com.niu.cntr.mapper.rolemapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;

import java.util.List;

/**
 * Created by xd on 2019/6/13.
 */
public class RoleServiceImpl implements RoleService {

    @Override
    public List<role> findAll() {
        rolemapper mapper = MapperFactory.createMapper(rolemapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.trade);
        List<role> rolelist  = mapper.findAll();
        return rolelist;
    }
}
