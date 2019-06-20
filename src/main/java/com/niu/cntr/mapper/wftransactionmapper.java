package com.niu.cntr.mapper;

import com.niu.cntr.entity.wftransaction;
import com.niu.cntr.mybatisConfig.Mapper;

public interface wftransactionmapper extends Mapper {
    Integer updateEndtradedate(wftransaction wftransaction);
}
