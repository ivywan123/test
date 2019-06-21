package com.niu.cntr.mapper;

import com.niu.cntr.mybatisConfig.Mapper;
import org.apache.ibatis.annotations.Param;

public interface t_cntr_posmapper extends Mapper {
    Integer updatePos(@Param("Stk_Cd") String Stk_Cd, @Param("Stk_Nm") String Stk_Nm,@Param("Cntr_Id")long Cntr_Id,@Param("Stk_Cd_pre")String Stk_Cd_pre);  //更新合约持仓
}
