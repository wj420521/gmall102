package cn.wj.gmall.manage.service.impl;

import cn.wj.gmall.bean.PmsProductImage;
import cn.wj.gmall.bean.PmsProductInfo;
import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.bean.PmsProductSaleAttrValue;
import cn.wj.gmall.manage.service.mapper.PmsProductImageMapper;
import cn.wj.gmall.manage.service.mapper.PmsProductInfoMapper;
import cn.wj.gmall.manage.service.mapper.PmsProductSaleAttrMapper;
import cn.wj.gmall.manage.service.mapper.PmsProductSaleAttrValueMapper;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsProductImageMapper productImageMapper;
    @Autowired
    PmsProductSaleAttrMapper productSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrValueMapper productSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> getSpuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
       //添加pmsProductInfo
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //插入图片
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(pmsProductInfo.getId());
            productImageMapper.insertSelective(pmsProductImage);
        }
        //插入销售属性
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            pmsProductSaleAttr.setProductId(pmsProductInfo.getId());
            productSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            //插入销售属性值
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());
                productSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
        return "success";
    }
    //查询销售属性列表
    @Override
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrs = productSaleAttrMapper.select(pmsProductSaleAttr);
        //查询销售属性值
        for (PmsProductSaleAttr productSaleAttr : productSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            //通过spuId和saleAttrId查询销售属性值列表
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> saleAttrValues = productSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(saleAttrValues);
        }
        return productSaleAttrs;
    }

    @Override
    public List<PmsProductImage> getSpuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> productImages = productImageMapper.select(pmsProductImage);
        return productImages;
    }

//    @Override
//    public List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(String productId) {
//        List<PmsProductSaleAttr> spuSaleAttrList = getSpuSaleAttrList(productId);
//        return spuSaleAttrList;
//    }

    @Override
    public List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(String productId,String skuId) {
        List<PmsProductSaleAttr> spuSaleAttrListCheckBySku = productSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId, skuId);
        return spuSaleAttrListCheckBySku;
    }

}
