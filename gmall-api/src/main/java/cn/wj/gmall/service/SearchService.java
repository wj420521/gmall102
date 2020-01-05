package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.SearchParam;

import java.util.List;

public interface SearchService {
    List<PmsSkuInfo> getSkuInfoList(SearchParam searchParam);

    public void hotScoreCount(String skuId);
}
