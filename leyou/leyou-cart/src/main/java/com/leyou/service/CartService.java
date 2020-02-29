package com.leyou.service;

import com.leyou.client.GoodsClient;
import com.leyou.common.utils.JsonUtils;
import com.leyou.interceptor.LoginInterceptor;
import com.leyou.item.pojo.Sku;
import com.leyou.pojo.Cart;
import com.leyou.pojo.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "leyou:cart:uid:";


    /**
     * 添加购物车
     *
     * @param cart
     */
    public void addCart(Cart cart) {

        //获取登录用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //Reids的key
        String redisKey = KEY_PREFIX + userInfo.getId();
        //获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(redisKey);

        //判断redis中是否有购物车记录
        Integer num = cart.getNum();
        String key = cart.getSkuId().toString();
        if (hashOps.hasKey(key)) {

            //有，修改数量
            String cartJson = hashOps.get(key).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(num);
        } else {
            //没有则新增
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setUserId(userInfo.getId());
        }
        hashOps.put(key, JsonUtils.serialize(cart));


    }

    public List<Cart> queryCarts() {

        //获取redis
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String id = KEY_PREFIX + userInfo.getId().toString();

        if (this.redisTemplate.hasKey(id)) {
            return null;
        }

        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(id);

        List<Object> carts = ops.values();
        //判断是否为空
        if (CollectionUtils.isEmpty(carts)) {
            return null;
        }

        //返回list
        return carts.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class)).collect(Collectors.toList());


    }
}
