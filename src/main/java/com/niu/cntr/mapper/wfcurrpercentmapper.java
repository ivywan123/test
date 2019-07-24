package com.niu.cntr.mapper;

import com.niu.cntr.entity.wfcurrpercent;
import com.niu.cntr.mybatisConfig.Mapper;
import org.apache.ibatis.annotations.Param;

public interface wfcurrpercentmapper extends Mapper {
    Integer updatewfcurrpercent(@Param("profit") long profit, @Param("id") long id); //更新合约市值表
}
