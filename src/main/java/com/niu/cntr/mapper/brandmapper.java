package com.niu.cntr.mapper;

import com.niu.cntr.entity.brand;
import com.niu.cntr.mybatisConfig.Mapper;

import java.util.List;

/**
 * Created by xd on 2019/6/12.
 */
public interface brandmapper extends Mapper {
    List<brand>  findAll();
}
