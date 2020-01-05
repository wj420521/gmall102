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
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    @Autowired
    RedisUtil redisUtil;
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

    //从缓存查询
    @Override
    public PmsSkuInfo getSkuInfo(String skuId) {
        Jedis jedis = null;
        PmsSkuInfo pmsSkuInfo = null;
        try{
            jedis = redisUtil.getJedis();
            if(jedis!=null){
                //能连接到redis,根据key PmsSkuInfo:skuId:info  从缓存中查询skuInfo信息
                String skuInfoJson = jedis.get("PmsSkuInfo:"+skuId+":info");
                if(StringUtils.isNotBlank(skuInfoJson)&&skuInfoJson!=""){
                    //通过key能查到
                    pmsSkuInfo = JSON.parseObject(skuInfoJson,PmsSkuInfo.class);
                }else{
                    //为空设置分布式缓存
                    //防止缓存击穿，，，一个存在的热点 key 高并发下 大量访问
                    String token = UUID.randomUUID().toString();
                    String lock = jedis.set("PmsSkuInfo:"+skuId+":lock", token, "NX", "EX", 10);
                    if(StringUtils.isNotBlank(lock)&&lock.equals("OK")){
                        //拿到锁了 次key存在时 别人不能再拿锁
                        //如过没连接到redis 或者缓存没查到
                        //从数据库查询
                        PmsSkuInfo skuInfoFromDb = getSkuInfoFromDb(skuId);
                        if(skuInfoFromDb!=null){
                            //添加进缓存 设置不同的缓存过期时间 防止缓存雪崩，，（因设置了相同的过期时间，到这大量key同时失效）
                            int i = new Random().nextInt(300);
                            jedis.setex("PmsSkuInfo:"+skuId+":info",60*60*24+i,JSON.toJSONString(skuInfoFromDb));
                        }else{
                            //数据库也没查到，设置个空串 进缓存 空缓存 防止 缓存穿透，访问不存在的skuId
                            jedis.setex("PmsSkuInfo:"+skuId+":info",60*3,"empty");
                        }
                        //释放锁  防止删除自己的key的时候 key刚过期，可以用lua脚本
                        String sToken = jedis.get("PmsSkuInfo:"+skuId+":lock");
                        if(StringUtils.isNotBlank(sToken)&&sToken.equals(token)){
                            jedis.del("PmsSkuInfo:"+skuId+":lock");
                        }
                    }else{
                        //没拿到锁 自旋
                         Thread.sleep(3000);
                        return getSkuInfo(skuId);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           jedis.close();
        }
        return pmsSkuInfo;
    }

    //从数据库查询
    public PmsSkuInfo getSkuInfoFromDb(String skuId) {
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


        //查询当前sku所属的spu下所有sku的属性值
    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueAndSkuIdBySpuId(String productId) {
        List<PmsSkuInfo> skuInfos = skuInfoMapper.selectSkuSaleAttrValueAndSkuIdBySpuId(productId);
        return skuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        List<PmsSkuInfo> skuInfos = skuInfoMapper.selectAll();
        for (PmsSkuInfo skuInfo : skuInfos) {
            String skuId = skuInfo.getId();
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

            skuInfo.setSkuImageList(pmsSkuImages);
            skuInfo.setSkuAttrValueList(pmsSkuAttrValues);
            skuInfo.setSkuSaleAttrValueList(pmsSkuSaleAttrValues);
        }
        return skuInfos;
    }
}
