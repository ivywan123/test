package com.niu.cntr.Service.TradeService;

import java.math.BigDecimal;

public interface T_cntrService {
    Integer updateProfit(BigDecimal profit, Long cntr_id);  //更新合约盈利
}
