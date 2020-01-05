package cn.wj.gmall.service;

import cn.wj.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    List<OmsCartItem> flushCache(String memberId);

    void updateCartItem(OmsCartItem omsCartItemDb);

    void addCartItem(OmsCartItem omsCartItem);

    List<OmsCartItem> getCartItemByMemberId(String memberId);

    List<OmsCartItem> getCartList(String memberId);
}
