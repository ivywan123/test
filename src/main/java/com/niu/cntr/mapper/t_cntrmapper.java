package com.niu.cntr.mapper;

import com.niu.cntr.entity.t_cntr;
import com.niu.cntr.mybatisConfig.Mapper;

import java.math.BigDecimal;

public interface t_cntrmapper extends Mapper {
    Integer updateProfit(BigDecimal profit,Long cntr_id);  //更新合约盈利

}
