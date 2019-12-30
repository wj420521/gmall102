package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsProductImage;
import cn.wj.gmall.bean.PmsProductInfo;
import cn.wj.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuList(String catalog3Id);

    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId);

    List<PmsProductImage> getSpuImageList(String spuId);
}
