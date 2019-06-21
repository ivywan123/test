package com.niu.cntr.Service.TradeDaoImpl;

import com.niu.cntr.Service.TradeService.T_cntr_posService;
import com.niu.cntr.mapper.t_cntr_posmapper;
import com.niu.cntr.mybatisConfig.DataSourceSqlSessionFactory;
import com.niu.cntr.mybatisConfig.MapperFactory;
import org.apache.ibatis.annotations.Param;

public class T_cntr_posServiceImpl implements T_cntr_posService {
    @Override
    public Integer updatePos(@Param("Stk_Cd") String Stk_Cd, @Param("Stk_Nm") String Stk_Nm, @Param("Cntr_Id")long Cntr_Id, @Param("Stk_Cd_pre")String Stk_Cd_pre){
        t_cntr_posmapper mapper = MapperFactory.createMapper(t_cntr_posmapper.class, DataSourceSqlSessionFactory.DataSourceEnvironment.trade);
        Integer result  = mapper.updatePos(Stk_Cd,Stk_Nm,Cntr_Id,Stk_Cd_pre);
        return result;
    }
}
