package com.niu.cntr.cntrsys;

import com.niu.cntr.func.Func;
import com.niu.cntr.inspect.Action;
import com.niu.cntr.inspect.SqlConnect;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

//合约展期预计算现金
public class ContractCalRenewpaidCashTest {
    Trade trade;
    Func func = new Func();

    @BeforeMethod
    public void setUp() {
        if(trade == null){
            trade = new Trade();
        }
        //准备待测合约
        //新增一个财云免息T+1的合约
        String productId = "52825297527251";
        Response re = func.trade_new(productId,5000,10,0);
        TradeVO.getInstance().setAccountId(re.path("trade.accountId"));
        TradeVO.getInstance().setBrandId(re.path("trade.brandId"));
        TradeVO.getInstance().setTradeId(re.path("trade.id"));
        TradeVO.getInstance().setCntrId(re.path("trade.tradeId"));
    }

    @AfterMethod
    public void tearDown() {
        func.trade_delete(TradeVO.getInstance().getTradeId(),TradeVO.getInstance().getAccountId());
    }

    @Test(groups = "smoke")
    //非到期日当天 13:00前操作
    public void testContracts_cal_renew_paidCash_noTime() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",TradeVO.getInstance().getTradeId());
        map.put("day",2);
        map.put("brandId",TradeVO.getInstance().getBrandId());
        map.put("accountId",TradeVO.getInstance().getAccountId());
        //验证预计算并断言
        Response paidcash = trade.contracts_cal_renew_paidCash(map);
        paidcash.then().body("success",equalTo(false));
        paidcash.then().body("errCode",equalTo("500429"));
        paidcash.then().body("status",equalTo("false"));
        paidcash.then().body("resultMsg",equalTo("请在到期日当天 23:59前操作！"));
    }

    @Test(groups = "smoke")
    //可正常计算延期
    public void testContracts_cal_renew_paidCash_normal() {
        HashMap<String, Object> map = new HashMap<>();
        SqlConnect sc = new SqlConnect();
        Long tradeId = TradeVO.getInstance().getTradeId();
//        Integer cntrId = TradeVO.getInstance().getCntrId();
        int day=2;
        //修改合约到期时间（当日）
        sc.update("cntrsys","update wftransaction set endTradeDate ='"+ Action.Time() +"' where id ="+tradeId+";");
        map.put("tradeId",tradeId);
        map.put("day",day);
        map.put("brandId",TradeVO.getInstance().getBrandId());
        map.put("accountId",TradeVO.getInstance().getAccountId());
        //查询合约详情 借款金额，产品有偿续约公式  为断言准备数据
        Response tradeRe = func.queryTrade(TradeVO.getInstance().getBrandId(),TradeVO.getInstance().getAccountId(),tradeId);
        Float borrowAmount =tradeRe.path("trade.borrowAmount");
        String paidRenewCost = tradeRe.path("trade.product.paidRenewCost");
        Long datVer = tradeRe.path("trade.product.datVer");
        Float pzMultiple = tradeRe.path("trade.pzMultiple");
        //获取倍率相应的有偿续约倍率
        Double[] renewPaid = {null};
        HashMap<String,Object> paidRenewCostm = Action.strTomap(paidRenewCost);
        paidRenewCostm.entrySet().forEach(entry -> {
            if(Math.abs(Float.parseFloat(entry.getKey()) - pzMultiple )>=0){
                Double d = Double.parseDouble(entry.getValue().toString());
                renewPaid[0] = borrowAmount * d * day;
            }
        });
        //验证预计算并断言
        Response paidcash = trade.contracts_cal_renew_paidCash(map);
        paidcash.then().body("success",equalTo(true));
        paidcash.then().body("renewPaid",equalTo(Float.parseFloat(renewPaid[0].toString())));
        paidcash.then().body("productDatVer",equalTo(datVer));
    }

    /**
    @Test(groups = "smoke")
    //非盈利不能延期卖出
    public void testContracts_cal_renew_paidCash_nomal() {
        HashMap<String, Object> map = new HashMap<>();
        SqlConnect sc = new SqlConnect();
        Long tradeId = tradeVO.getTradeId();
        Long cntrId = tradeVO.getCntrId();
        //修改合约到期时间（当日）
        sc.update("cntrsys","update wftransaction set endTradeDate ="+ Action.Time() +" where id ="+tradeId+";");
        //改造合约盈利100
        sc.update("niudb","UPDATE t_cntr SET Cur_Bal_Amt =Cur_Bal_Amt+100,Cur_Aval_Cap_Amt=Cur_Aval_Cap_Amt+100,Cur_Tt_Ast_Amt=Cur_Tt_Ast_Amt+100 WHERE Cntr_Id ="+ cntrId);
        map.put("tradeId",tradeVO.getTradeId());
        map.put("day",2);
        map.put("brandId",tradeVO.getBrandId());
        map.put("accountId",tradeVO.getAccountId());
        //验证预计算并断言
        Response paidcash = trade.contracts_cal_renew_paidCash(map);
        paidcash.then().body("success",equalTo(false));
        paidcash.then().body("errCode",equalTo(500429));
        paidcash.then().body("status",equalTo("false"));
        paidcash.then().body("resultMsg",equalTo("请在到期日当天 13:00前操作！"));
    }
**/

}