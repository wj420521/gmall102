package cn.wj.gmall.item.controller;

import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.PmsSkuSaleAttrValue;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("/{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId")String skuId, ModelMap map, HttpServletRequest request,String valueIds){

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfo(skuId);
        String productId = pmsSkuInfo.getProductId();
        List<PmsProductSaleAttr> productSaleAttrs =  spuService.getSpuSaleAttrListCheckBySku(productId,skuId);

        //获取当前sku下的spu关联的所有sku销售属性值组合
        Map<String,String> hashJsonMap = new HashMap<>();
        String key="";
        String value="";
        List<PmsSkuInfo> skuInfos = skuService.getSkuSaleAttrValueAndSkuIdBySpuId(productId);
        for (PmsSkuInfo skuInfo : skuInfos) {
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            int count = 1;
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                if(count>1){
                    key += pmsSkuSaleAttrValue.getSaleAttrValueId();
                }else{
                    key+=pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
                }
                count++;
            }
            hashJsonMap.put(key,skuInfo.getId());
            key = "";
        }
        String hashJsonSku = JSON.toJSONString(hashJsonMap);
        map.put("valuesSku",hashJsonSku);
        map.put("skuInfo",pmsSkuInfo);
        map.put("spuSaleAttrListCheckBySku",productSaleAttrs);

        return "item";
    }

}

