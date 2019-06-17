package com.niu.cntr.Service.CntrDaoImpl;

import com.niu.cntr.Service.CntrService.BrandService;
import com.niu.cntr.entity.brand;
import com.niu.cntr.mapper.brandmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;

import java.util.List;

/**
 * Created by xd on 2019/6/12.
 */
public class brandServiceImpl implements BrandService {
    @Override
    public List<brand> findAll() {
        brandmapper mapper = MapperFactory.createMapper(brandmapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.cntr);
        List<brand> brandlist  = mapper.findAll();
        return brandlist;
    }

    @Override
    public brand findById(long brandId) {
        return null;
    }

    @Override
    public int deleteById(long brandId) {
        return 0;
    }

    @Override
    public int insert(brand brand) {
        return 0;
    }

    @Override
    public int update(brand brand) {
        return 0;
    }
}
