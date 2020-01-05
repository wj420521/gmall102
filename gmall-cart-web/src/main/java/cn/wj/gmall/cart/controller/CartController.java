package cn.wj.gmall.cart.controller;

import cn.wj.gmall.bean.OmsCartItem;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;


    @RequestMapping("checkCart")
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = "1";
        List<OmsCartItem> cartItemList = new ArrayList<>();
        if (memberId == "1") {
            //登陆修改数据库
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            omsCartItem.setProductSkuId(skuId);
            omsCartItem.setIsChecked(isChecked);
            cartService.updateCartItem(omsCartItem);
            //更新页面 缓存
            cartItemList = cartService.flushCache(memberId);
        } else {
            //没登录修改cookie
            String cookieValue = CookieUtil.getCookieValue(request, "cartCookie", true);
            cartItemList =  JSON.parseArray(cookieValue,OmsCartItem.class);
            for (OmsCartItem omsCartItem : cartItemList) {
                if (omsCartItem.getProductSkuId().equals(skuId)) {
                    omsCartItem.setIsChecked(isChecked);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request, response, "cartCookie", JSON.toJSONString(cartItemList), 1000 * 60 * 60 * 24, true);
        }

        //修改购物车状态
        modelMap.put("totalAmount", getTotalAmount(memberId,request));
        modelMap.put("cartList",cartItemList);
        return "inner";
    }

    @RequestMapping("cartList")
    public String cartList(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        //模拟登陆
        String memberId = "1";
        List<OmsCartItem> cartItemList = new ArrayList<>();
        //根据用户id查询购物车列表
        if (memberId == "1") {
            cartItemList = cartService.getCartList(memberId);
        } else {
            //如果没登陆 从Cookie查询
            String cookieValue = CookieUtil.getCookieValue(request, "cartCookie", true);
            if (StringUtils.isNotBlank(cookieValue)) {
                cartItemList =  JSON.parseArray(cookieValue,OmsCartItem.class);
            }
        }
        modelMap.put("totalAmount", getTotalAmount(memberId,request));
        modelMap.put("cartList", cartItemList);
        return "cartList";
    }

    //商品详情页 添加购物车
    @RequestMapping("addToCart")
    public String addToCart(String skuId, String num, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {

        //模拟登陆
        String memberId = "1";
        //根据skuid查询
        PmsSkuInfo skuInfo = skuService.getSkuInfo(skuId);
        modelMap.put("skuInfo", skuInfo);
        modelMap.put("skuNum", num);

        //转换成购物车对象
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(skuInfo.getId());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setQuantity(new BigDecimal(num));
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setIsChecked("0");

        if (memberId == "1") {
            omsCartItem.setMemberId(memberId);
            omsCartItem.setMemberNickname("");
            //如果登陆了 查询cookie
            //合并购物车
            mergeCart(memberId, request, response);
            //查询数据库 看有没有
            List<OmsCartItem> omsCartItemDb = cartService.getCartItemByMemberId(memberId);
            if (omsCartItemDb != null) {
                //数据库有数据
                int count = 1;
                for (OmsCartItem item : omsCartItemDb) {
                    //如果相等
                    if (item.getProductSkuId().equals(skuId)) {
                        //更新数量
                        item.setQuantity(item.getQuantity().add(new BigDecimal(num)));
                        //更新数据库
                        cartService.updateCartItem(item);
                        count++;
                    }
                }
                if (count == 1) {
                    //说明没有相同的
                    //加入数据库
                    cartService.addCartItem(omsCartItem);
                }
            } else {
                //数据库为空
                //加入数据库
                cartService.addCartItem(omsCartItem);
            }
            //刷新緩存
            cartService.flushCache(memberId);
        } else {
            //如果没登录
            //查询cookie中有没有该商品 有则修改数量，没有则添加
            List<OmsCartItem> cartItems = new ArrayList<>();
            String cookieValue = CookieUtil.getCookieValue(request, "cartCookie", true);
            //cookie有数据
            if (StringUtils.isNotBlank(cookieValue)) {
                cartItems = JSON.parseArray(cookieValue,OmsCartItem.class);
                int count = 1;
                for (OmsCartItem cartItem : cartItems) {
                    if (cartItem.getProductSkuId().equals(skuId)) {
                        //有相同的skuId
                        //修改数量
                        cartItem.setQuantity(cartItem.getQuantity().add(new BigDecimal(num)));
                        count++;
                    }
                }
                if (count == 1) {
                    //count=1则说明没有相同的skuId
                    //没有相同的skuId
                    //直接添加进去
                    cartItems.add(omsCartItem);
                }
                //更新cookie
                CookieUtil.setCookie(request, response, "cartCookie", JSON.toJSONString(cartItems), 1000 * 60 * 60 * 24, true);
            } else {
                //cookie没数据
                //直接添加进去
                cartItems.add(omsCartItem);
                //更新cookie
                CookieUtil.setCookie(request, response, "cartCookie", JSON.toJSONString(cartItems), 1000 * 60 * 60 * 24, true);
            }
        }
        return "success";
    }

    //合并购物车
    public void mergeCart(String memberId, HttpServletRequest request, HttpServletResponse response) {
        //此时时已经登陆了
        //查询cookie
        String cookieValue = CookieUtil.getCookieValue(request, "cartCookie", true);
        //查询数据库 看有没有
        List<OmsCartItem> omsCartItemDb = cartService.getCartItemByMemberId(memberId);
        if (StringUtils.isNotBlank(cookieValue)) {
            // cookie有数据 合并购物车
            List<OmsCartItem> cartItems = JSON.parseArray(cookieValue,OmsCartItem.class);
            if (omsCartItemDb != null) {
                //数据库有数据
                //cookie
                for (OmsCartItem cartItem : cartItems) {
                    //数据库
                    for (OmsCartItem item : omsCartItemDb) {
                        //如果相等
                        if (cartItem.getProductSkuId().equals(item.getProductSkuId())) {
                            //修改数量
                            cartItem.setQuantity(cartItem.getQuantity().add(item.getQuantity()));
                            //更新数据库
                            cartService.updateCartItem(item);
                            //移除cookie的一条数据
                            cartItems.remove(cartItem);
                        }
                    }
                }
                //如果此时cookie还有数据
                if (cartItems != null) {
                    //全加入数据库
                    for (OmsCartItem cartItem : cartItems) {
                        cartItem.setMemberId(memberId);
                        cartItem.setMemberNickname("");
                        cartService.addCartItem(cartItem);
                    }
                }
            } else {
                //数据库空 cookie全加入
                for (OmsCartItem cartItem : cartItems) {
                    cartItem.setMemberId(memberId);
                    cartItem.setMemberNickname("");
                    cartService.addCartItem(cartItem);
                }
            }
            //删除cookie
            CookieUtil.deleteCookie(request, response, "cartCookie");
        }
    }

    //计算总价
    public String getTotalAmount(String memberId, HttpServletRequest request) {
        BigDecimal totalAmount = new BigDecimal(0);
        if (memberId == "1") {
            //登陆
            List<OmsCartItem> cartList = cartService.getCartList(memberId);
            for (OmsCartItem omsCartItem : cartList) {
                if (omsCartItem.getIsChecked().equals("1")) {
                   totalAmount = totalAmount.add(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()));
                }
            }
        } else {
            //cookie
            String cookieValue = CookieUtil.getCookieValue(request, "cartCookie", true);
            List<OmsCartItem> list = JSON.parseArray(cookieValue, OmsCartItem.class);
            for (OmsCartItem omsCartItem : list) {
                if (omsCartItem.getIsChecked() == "1") {
                    totalAmount = totalAmount.add(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()));
                }
            }
        }
        return totalAmount.toString();
    }

    @RequestMapping("toTrade")
    public String toTrade(){
        String memberId ="1";
        if(memberId=="1"){
            //登陆了 重定向到 order/..
            return "trade";
        }else{
            //没登陆 拦截去登陆
            return "tradeFile";
        }

    }

}