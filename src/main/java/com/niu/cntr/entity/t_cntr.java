package com.niu.cntr.entity;

import java.math.BigDecimal;

public class t_cntr {
    private Long Cntr_Id;
    private BigDecimal Cur_Bal_Amt;  //当前余额
    private BigDecimal Cur_Aval_Cap_Amt;  //当前可用资金金额
    private BigDecimal Cur_Tt_Ast_Amt; //当前总资产金额

    public Long getCntr_Id() {
        return Cntr_Id;
    }

    public void setCntr_Id(Long cntr_Id) {
        Cntr_Id = cntr_Id;
    }

    public BigDecimal getCur_Bal_Amt() {
        return Cur_Bal_Amt;
    }

    public void setCur_Bal_Amt(BigDecimal cur_Bal_Amt) {
        Cur_Bal_Amt = cur_Bal_Amt;
    }

    public BigDecimal getCur_Aval_Cap_Amt() {
        return Cur_Aval_Cap_Amt;
    }

    public void setCur_Aval_Cap_Amt(BigDecimal cur_Aval_Cap_Amt) {
        Cur_Aval_Cap_Amt = cur_Aval_Cap_Amt;
    }

    public BigDecimal getCur_Tt_Ast_Amt() {
        return Cur_Tt_Ast_Amt;
    }

    public void setCur_Tt_Ast_Amt(BigDecimal cur_Tt_Ast_Amt) {
        Cur_Tt_Ast_Amt = cur_Tt_Ast_Amt;
    }
}
