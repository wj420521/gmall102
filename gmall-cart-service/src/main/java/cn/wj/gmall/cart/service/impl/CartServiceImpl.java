package cn.wj.gmall.cart.service.impl;

import cn.wj.gmall.bean.OmsCartItem;
import cn.wj.gmall.cart.service.mapper.OmsCartItemMapper;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;
    //根据用户名和skuId查询购物车
    @Override
    public List<OmsCartItem> getCartItemByMemberId(String memberId) {
      OmsCartItem omsCartItem = new OmsCartItem();
      omsCartItem.setMemberId(memberId);
        List<OmsCartItem> cartItems = omsCartItemMapper.select(omsCartItem);
        return cartItems;
    }

    //更新购物车
    @Override
    public void updateCartItem(OmsCartItem omsCartItemDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItemDb.getMemberId()).andEqualTo("productSkuId",omsCartItemDb.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemDb,example);
    }
    //添加
    @Override
    public void addCartItem(OmsCartItem omsCartItem) {
        omsCartItemMapper.insertSelective(omsCartItem);
    }
    //添加更新缓存
    @Override
    public List<OmsCartItem> flushCache(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        //没有则查询数据库
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        //加入缓存
        jedis.set("cartCache:"+memberId, JSON.toJSONString(omsCartItems));
        jedis.close();
        return omsCartItems;
    }
    //查询购物车列表
    public List<OmsCartItem> getCartList(String memberId){
        Jedis jedis = redisUtil.getJedis();
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if(jedis!=null){
            String cart = jedis.get("cartCache:" + memberId);
            if(StringUtils.isNotBlank(cart)){
                //如果缓存有
                omsCartItems = JSON.parseArray(cart, OmsCartItem.class);
            }else{
                //没有则查询数据库
                OmsCartItem omsCartItem = new OmsCartItem();
                omsCartItem.setMemberId(memberId);
                omsCartItems = omsCartItemMapper.select(omsCartItem);
                //加入缓存
                jedis.set("cartCache:"+memberId, JSON.toJSONString(omsCartItems));
            }
        }
        jedis.close();
        return omsCartItems;
    }
}
