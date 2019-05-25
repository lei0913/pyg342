package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/index")
public class indexController {

    /**
     * 根据广告分类id, 查询广告集合数据返回
     *
     * @param categoryId
     * @return
     */
    @Reference
    private ItemCatService itemCatService;

    //网站前台商品分类展示
    @RequestMapping("/findItemCatList")
    public List<ItemCat> findItemCatList(Long categoryId) {
        return itemCatService.findItemCatList();
    }
}
