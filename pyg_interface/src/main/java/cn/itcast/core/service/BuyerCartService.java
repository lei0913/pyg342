package cn.itcast.core.service;

import cn.itcast.core.pojo.collection.Collection;
import cn.itcast.core.pojo.entity.BuyerCart;

import java.util.List;

public interface BuyerCartService {

    /**
     * 添加购买的商品到这个人拥有的购物车列表中
     * @param cartList  现在所拥有的购物车列表
     * @param itemId    库存id
     * @param num       购买数量
     * @return    返回添加后的购物车列表
     */
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num);

    /**
     * 将购物车列表添加到redis中保存
     * @param userName  登录用户的用户名
     * @param cartList  用户的购物车列表
     */
    public void setCartListToRedis(String userName, List<BuyerCart> cartList);

    /**
     * 通过用户名获取用户的购物车列表
     * @param userName  用户名
     * @return
     */
    public List<BuyerCart> getCartListFromRedis(String userName);

    /**
     * 将cookie中的购物车集合合并到redis的购物车集合中返回
     * @param cookieCartList    cookie中的购物车集合
     * @param redisCartList     redis中的购物车集合
     * @return
     */
    public List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList);

    /**
     * 添加商品到收藏
     * @param userId    用户 ID
     * @param ids   商品ID集合
     */
     void addGoodsToCollection(String userId,Long[] ids);

    /**
     * 根据用户名查询商品id
     * @param username  用户名
     * @return           商品id集合
     */
    List<Collection>findGoodsId(String username);

    /**
     * 判断数据库中是否有当前商品
     * @param goodsIds      数据库收藏信息集合信息集合
     * @param ids           前端商品id集合
     * @return               是否存在
     */
    boolean isExist(List<Collection> goodsIds , Long[] ids);


}


