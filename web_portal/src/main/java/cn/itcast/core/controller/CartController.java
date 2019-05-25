package cn.itcast.core.controller;

import cn.itcast.core.pojo.collection.Collection;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.BuyerCartService;
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.io.ResolverUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车业务
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private BuyerCartService buyerCartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    /**
     * 添加商品到购物车中
     * @param itemId    购买商品的库存id
     * @param num       购买数量
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    //解决跨域访问, origins="http://localhost:8086"指定响应返回到哪台服务器
    @CrossOrigin(origins="http://localhost:8086",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //1. 获取当前登录用户名称
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 获取购物车列表
        List<BuyerCart> cartList = findCartList();
        //3. 将当前商品加入到购物车列表
        cartList = buyerCartService.addItemToCartList(cartList, itemId, num);
        //4. 判断当前用户是否登录, 未登录用户名为"anonymousUser"
        if ("anonymousUser".equals(userName)) {
            //4.a.如果未登录, 则将购物车列表存入cookie中
            String carListJsonStr = JSON.toJSONString(cartList);
            CookieUtil.setCookie(request, response, Constants.COOKIE_CARTLIST, carListJsonStr, 3600 * 24 * 30, "utf-8");
        } else {
            //4.b.如果已登录, 则将购物车列表存入redis中
            buyerCartService.setCartListToRedis(userName, cartList);
        }
        return new Result(true, "添加成功!");
    }

    /**
     * 查询当前用户的购物车列表数据并返回
     * @return
     */
    @RequestMapping("/findCartList")
    public List<BuyerCart> findCartList() {
        //1. 获取当前登录用户名称
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 从cookie中获取购物车列表json格式字符串
        String cookieCartListJsonStr = CookieUtil.getCookieValue(request, Constants.COOKIE_CARTLIST, "utf-8");
        //3. 如果购物车列表json串为空则返回"[]"
        if (cookieCartListJsonStr == null || "".equals(cookieCartListJsonStr)) {
            cookieCartListJsonStr = "[]";
        }
        //4. 将购物车列表json转换为对象
        List<BuyerCart> cookieCartList = JSON.parseArray(cookieCartListJsonStr, BuyerCart.class);
        //5. 判断用户是否登录, 未登录用户为"anonymousUser"
        if ("anonymousUser".equals(userName)) {
            //5.a. 未登录, 返回cookie中的购物车列表对象
            return cookieCartList;
        } else {
            //5.b.1.已登录, 从redis中获取购物车列表对象
            List<BuyerCart> redisCartList = buyerCartService.getCartListFromRedis(userName);
            //5.b.2.判断cookie中是否存在购物车列表
            if (cookieCartList != null && cookieCartList.size() > 0) {
                //如果cookie中存在购物车列表则和redis中的购物车列表合并成一个对象
                redisCartList = buyerCartService.mergeCookieCartListToRedisCartList(cookieCartList, redisCartList);
                //删除cookie中购物车列表
                CookieUtil.deleteCookie(request, response, Constants.COOKIE_CARTLIST);
                //将合并后的购物车列表存入redis中
                buyerCartService.setCartListToRedis(userName, redisCartList);
            }
            //5.b.3.返回购物车列表对象
            return redisCartList;
        }
    }

    /**
     * 购物车收藏
     * @param ids   商品的id
     * @return      添加的结果
     */
    @RequestMapping("/addGoodsToCollection")
    public Result addGoodsToCollection(Long[] ids){

        try {
//        1、获取当前用户的用户名
         String userName = SecurityContextHolder.getContext().getAuthentication().getName();
//        2、判断出入的数据是否为空
            if (ids==null&&ids.length==0){
                return new Result(false,"请重新选择商品");
            }
//        3、判断用户是否登录
            if ("anonymousUser".equals(userName)) {

                return new Result(false,"请登录");
            }


//        4、查询数据库中当前用户的数据
            List<Collection> goodsId = buyerCartService.findGoodsId(userName);
//        5、查询是否与数据中冲突
            boolean exist = buyerCartService.isExist(goodsId, ids);
            if (exist) {
//                6、如果没有相同数据，则添加
                buyerCartService.addGoodsToCollection(userName, ids);

//
                return new Result(true, "添加成功");
            }else {
                return new Result(false,"商品已存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            //        4、如果添加失败，返回添加失败
            return new Result(false,"商品添加失败");
        }



    }



}
