package cn.wj.gmall.manage.service.impl;

import cn.wj.gmall.bean.PmsSkuAttrValue;
import cn.wj.gmall.bean.PmsSkuImage;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.PmsSkuSaleAttrValue;
import cn.wj.gmall.manage.service.mapper.PmsSkuAttrValueMapper;
import cn.wj.gmall.manage.service.mapper.PmsSkuImageMapper;
import cn.wj.gmall.manage.service.mapper.PmsSkuInfoMapper;
import cn.wj.gmall.manage.service.mapper.PmsSkuSaleAttrValueMapper;
import cn.wj.gmall.service.SkuService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper skuInfoMapper;
    @Autowired
    PmsSkuImageMapper skuImageMapper;
    @Autowired
    PmsSkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper skuSaleAttrValueMapper;
    //添加sku
    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //添加sku
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        skuInfoMapper.insertSelective(pmsSkuInfo);
        //添加sku图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            skuImageMapper.insertSelective(pmsSkuImage);
        }
        //添加sku属性值
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            skuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //添加sku销售属性值
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        return "success";
    }
}
