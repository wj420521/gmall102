package cn.wj.gmall.service;

import cn.wj.gmall.bean.*;

import java.util.List;

public interface PmsBaseService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);

    List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    void addPmsBaseAttr(PmsBaseAttrInfo pmsBaseAttrInfo);

    void updatePmsBaseAttr(PmsBaseAttrInfo pmsBaseAttrInfo);

    void deletePmsBaseAttr(String attrId);
}
