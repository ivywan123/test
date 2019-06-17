package com.niu.cntr.Service.CntrService;

import com.niu.cntr.entity.brand;

import java.util.List;

/**
 * Created by xd on 2019/6/12.
 */
public interface BrandService {
    List<brand> findAll();

    brand findById(long brandId);

    int deleteById(long brandId);

    int insert(brand brand);

    int update(brand brand);
}
