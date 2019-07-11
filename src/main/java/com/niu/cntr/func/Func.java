package com.niu.cntr.func;

import com.niu.cntr.CntrConfig;
import com.niu.cntr.cntrsys.Product;
import com.niu.cntr.cntrsys.Trade;
import com.niu.cntr.cntrsys.TradeClient;
import com.niu.cntr.inspect.Action;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class Func {

    /**
    public static void  main(String[] args) throws IOException {
        Response re = trade_reduce(50190516102039L,53897715489425L,100);
        re.then().body("success",equalTo(true));
    }
     **/

    //新增合约功能封装
    public Response trade_new(String productId, long borrowAmount, long pzMultiple, double deductionAmount){
        Product product = new Product();
        JsonPath js = new JsonPath(product.queryone(productId, CntrConfig.getInstance().brandId).asString());
        List<Long> datavers = js.getList("product.datVer");
        if(datavers != null){
            String dataver = String.valueOf(datavers.get(0));
            //产品数据版本，initiDuration
            List<String> cycleOptions = js.getList("product.cycleOptions");
            String initiDuration = cycleOptions.get(0).split(",")[0];
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", Action.random());
            map.put("datVer", dataver);
            map.put("productId", productId);
            map.put("initiDuration", initiDuration);
            map.put("pzMultiple", pzMultiple);
            map.put("borrowAmount", borrowAmount);
            map.put("deductionAmount", deductionAmount);
            map.put("accountId", Action.random());
            map.put("phoneNumber", Action.getTelephone());
            return product.contract_create(map);
        }
        else {
            return null;
        }
    }

    //放大功能封装
    public Response trade_leverCapital(Long tradeId,Long accountId,double capitalAmount,float deductionAmount,boolean flag ){
        Trade trade = new Trade();
        HashMap<String, Object> map = new HashMap<>();
        //查询合约详情
        Response re = queryTrade(CntrConfig.getInstance().brandId,accountId,tradeId);
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("capitalAmount",capitalAmount); //放大金额
        map.put("flag",flag);  //是否使用可提现金
        map.put("datVer",re.path("trade.product.datVer"));
        map.put("id",Action.random());
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("deductionAmount",deductionAmount);
        return trade.contracts_leverCapital(map);
    }

    //缩小功能封装
    public Response trade_reduce(Long tradeId, Long accountId, double borrowAmount){
        //查询合约详情
        Trade trade = new Trade();
        Response re = queryTrade(CntrConfig.getInstance().brandId,accountId,tradeId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("borrowAmount",borrowAmount);
        map.put("datVer",re.path("trade.product.datVer"));
        map.put("id",Action.random());
        map.put("brandId", CntrConfig.getInstance().brandId);
        return trade.contracts_reduceCapital(map);
    }

    //追加非杠杆功能封装
    public Response trade_Capital(Long tradeId, Long accountId, double capitalAmount){
        //查询合约详情
        Trade trade = new Trade();
        Response re = queryTrade(CntrConfig.getInstance().brandId,accountId,tradeId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("tradeId",tradeId);
        map.put("accountId",accountId);
        map.put("capitalAmount",capitalAmount);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("id",Action.random());
        return trade.contracts_capital(map);
    }

    //提取现金功能封装
    public Response trade_fetchCash(Long tradeId,Long accountId,double cash,boolean flag){
        //查询合约详情
        Trade trade = new Trade();
//        Response re = queryTrade(CntrConfig.getInstance().brandId,accountId,tradeId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id",Action.random());
        map.put("accountId",accountId);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("cash",cash);
        map.put("flag",flag);
        map.put("tradeId",tradeId);
        return trade.contracts_fetchCash(map);
    }

    //终止合约功能封装
    public Response trade_delete(Long tradeId, Long accountId){
       //查询合约详情
        Trade trade = new Trade();
        Response re = queryTrade(CntrConfig.getInstance().brandId,accountId,tradeId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id",Action.random());
        map.put("accountId",accountId);
        map.put("brandId", CntrConfig.getInstance().brandId);
        map.put("tradeId",tradeId);
        return trade.contracts_delete(map);
    }

    //下委托功能封装
    public Response sendOrder(Long memberId,Long tradeId,String stockCode,Integer qty){
        HashMap<String, Object> map = new HashMap<>();
        Float price = null;
        TradeClient tradeClient = new TradeClient();
        Response re = tradeClient.queryStockPrice(stockCode,CntrConfig.getInstance().brandId);
        price = re.path("stockPrice.sellPrice1");
        map.put("memberId",memberId);
        map.put("tradeId",tradeId);
        map.put("stockCode",stockCode);
        map.put("brandId",CntrConfig.getInstance().brandId);
        map.put("qty",qty);
        map.put("price",price);
        map.put("orderSide","B");
        map.put("orderType","H");
        return tradeClient.sendOrder(map);
    }

    //查询股票名称
    public Response queryStkNm(String stkCd,Long brandId){
        TradeClient tradeClient = new TradeClient();
        return tradeClient.queryStkNm(stkCd,brandId);
    }

    //查询合约详情封装
    public Response queryTrade(Long brandId, Long memberId,Long tradeId){
        Trade trade = new Trade();
        return trade.contracts_queryContractDetail(brandId,memberId,tradeId);
    }
}
