package cn.wj.gmall.manage.service.mapper;

import cn.wj.gmall.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    List<PmsSkuInfo> selectSkuSaleAttrValueAndSkuIdBySpuId(@Param("productId") String productId);

}
