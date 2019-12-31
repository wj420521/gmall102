package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfo(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueAndSkuIdBySpuId(String productId);
}
