package cn.wj.gmall.manage.service.impl;

import cn.wj.gmall.bean.*;
import cn.wj.gmall.manage.service.mapper.*;
import cn.wj.gmall.service.PmsBaseService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class PmsBaseServiceImpl implements PmsBaseService {

    @Autowired
    PmsBaseCatalog1Mapper catalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper catalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper catalog3Mapper;
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    //查询一级分类列表
    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        List<PmsBaseCatalog1> pmsBaseCatalog1s = catalog1Mapper.selectAll();
        return pmsBaseCatalog1s;
    }

    //根据一级分类id查询二级分类列表
    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        List<PmsBaseCatalog2> list = catalog2Mapper.select(pmsBaseCatalog2);
        return list;
    }

    //根据二级分类id查询三级分类列表
    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> list = catalog3Mapper.select(pmsBaseCatalog3);
        return list;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        //将属性值设置进去
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> select = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(select);
        }
        return pmsBaseAttrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> select = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return select;
    }

    //添加平台属性
    @Override
    public void addPmsBaseAttr(PmsBaseAttrInfo pmsBaseAttrInfo) {
        //添加平台属性
        pmsBaseAttrInfoMapper.insert(pmsBaseAttrInfo);
        //添加平台属性值
        //主键返回
        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.insert(pmsBaseAttrValue);
        }

    }

    @Override
    public void updatePmsBaseAttr(PmsBaseAttrInfo pmsBaseAttrInfo) {
        //修改属性
        Example example = new Example(PmsBaseAttrInfo.class);
        example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
        PmsBaseAttrInfo pmsBaseAttrInfo1 = new PmsBaseAttrInfo();
        pmsBaseAttrInfo1.setCatalog3Id(pmsBaseAttrInfo.getCatalog3Id());
        pmsBaseAttrInfo1.setAttrName(pmsBaseAttrInfo.getAttrName());
        pmsBaseAttrInfo1.setIsEnabled(pmsBaseAttrInfo.getIsEnabled());
        pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo1,example);
        //修改属性值(也可以写成，先删除，在插入)
        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
            //先删除
            deletePmsBaseAttr(pmsBaseAttrValue.getAttrId());
             //插入
            pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
        }

    }
    public void deletePmsBaseAttr(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
    }

    @Override
    public List<PmsBaseSaleAttr> getBaseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrs;
    }
}
