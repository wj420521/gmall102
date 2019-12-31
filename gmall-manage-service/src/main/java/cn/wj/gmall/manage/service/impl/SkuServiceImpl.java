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

    @Override
    public PmsSkuInfo getSkuInfo(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        //查询sku信息
        PmsSkuInfo pmsSkuInfo1 = skuInfoMapper.selectOne(pmsSkuInfo);
        //查询当前skuId的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = skuImageMapper.select(pmsSkuImage);
        //查询属性值
        PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
        pmsSkuAttrValue.setSkuId(skuId);
        List<PmsSkuAttrValue> pmsSkuAttrValues = skuAttrValueMapper.select(pmsSkuAttrValue);
        //查询销售属性
        PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
        pmsSkuSaleAttrValue.setSkuId(skuId);
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = skuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);

        pmsSkuInfo1.setSkuImageList(pmsSkuImages);
        pmsSkuInfo1.setSkuAttrValueList(pmsSkuAttrValues);
        pmsSkuInfo1.setSkuSaleAttrValueList(pmsSkuSaleAttrValues);

        return pmsSkuInfo1;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueAndSkuIdBySpuId(String productId) {
        List<PmsSkuInfo> skuInfos = skuInfoMapper.selectSkuSaleAttrValueAndSkuIdBySpuId(productId);
        return skuInfos;
    }
}
