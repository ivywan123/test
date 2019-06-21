package com.niu.cntr.mapper;

import com.niu.cntr.entity.t_cntr;
import com.niu.cntr.mybatisConfig.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface t_cntrmapper extends Mapper {
    Integer updateProfit(@Param("profit") long profit, @Param("id") long cntr_id);  //更新合约盈利

}
