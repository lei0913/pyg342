package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements  ItemCatService {

    @Autowired
    private ItemCatDao catDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        /**
         * 缓存分类数据到redis中
         */
        //1. 查询所有分类表数据
        List<ItemCat> catList = catDao.selectByExample(null);
        if (catList != null) {
            for (ItemCat itemCat : catList) {
                redisTemplate.boundHashOps(Constants.REDIS_CATEGORYLIST).put(itemCat.getName(), itemCat.getTypeId());
            }
        }

        /**
         * 2. 根据父级id进行查询
         */
        ItemCatQuery query = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = query.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<ItemCat> list = catDao.selectByExample(query);
        return list;
    }

    @Override
    public ItemCat findOne(Long id) {
        return catDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ItemCat> findAll() {
        return catDao.selectByExample(null);
    }


         @Override
         public List<ItemCat> findByItemCat3(Long parentId01) {
            // 1.先从redis缓存中 , 获取三级分类信息!
            List<ItemCat> itemCat01List  = (List<ItemCat>) redisTemplate.boundValueOps(Constants.REDIS_itemCat03).get();

            // 2.若缓存中没有数据 , 从数据库中查询( 并放到缓存中 )
            if (itemCat01List==null){
                // 缓存穿透 -> 请求排队等候.
                synchronized (this){
                    // 进行二次校验?
                    itemCat01List  = (List<ItemCat>) redisTemplate.boundValueOps(Constants.REDIS_itemCat03).get();
                    if (itemCat01List==null){
                        // 创建一个集合 , 存放一级分类
                        itemCat01List = new ArrayList<>();

                        // 根据parent_id = 0 , 获取一级分类信息!
                        List<ItemCat> itemCatList = catDao.selectByParentId(parentId01);
                        for (ItemCat itemCat : itemCatList) {
                            // 设置一级分类信息!
                            ItemCat itemCat01 = new ItemCat();
                            itemCat01.setId(itemCat.getId());
                            itemCat01.setName(itemCat.getName());
                            itemCat01.setParentId(itemCat.getParentId());

                            // 根据一级分类的id -> 找到对应的二级分类!
                            List<ItemCat> itemCatList02 = new ArrayList<>();
                            ItemCatQuery itemCatQuery02 = new ItemCatQuery();
                            itemCatQuery02.createCriteria().andParentIdEqualTo(itemCat.getId());
                            List<ItemCat> itemCat02List = catDao.selectByExample(itemCatQuery02);
                            for (ItemCat itemCat2 : itemCat02List) {
                                // 设置二级分类信息!
                                ItemCat itemCat02 = new ItemCat();
                                itemCat02.setId(itemCat2.getId());
                                itemCat02.setName(itemCat2.getName());
                                itemCat02.setParentId(itemCat2.getParentId());


                                // 根据二级分类的id -> 找到对应的三级分类!
                                List<ItemCat> itemCatList03 = new ArrayList<>();
                                ItemCatQuery itemCatQuery03 = new ItemCatQuery();
                                itemCatQuery03.createCriteria().andParentIdEqualTo(itemCat02.getId());
                                List<ItemCat> itemCat03List = catDao.selectByExample(itemCatQuery03);
                                for (ItemCat itemCat3 : itemCat03List) {
                                    itemCatList03.add(itemCat3);
                                }
                                itemCat02.setItemCatList(itemCatList03);  // 二级分类中 添加 三级分类.
                                itemCatList02.add(itemCat02);       // 添加二级分类.
                            }
                            itemCat01.setItemCatList(itemCatList02); // 一级分类中 添加 二级分类!
                            itemCat01List.add(itemCat01);  // 添加一级分类
                        }
                        // 将查询到的数据放入缓存中!
                        redisTemplate.boundValueOps(Constants.REDIS_itemCat03).set(itemCat01List);
                        return itemCat01List;
                    }
                }

            }

            // 3.若缓存中有数据 , 直接返回即可!
            return itemCat01List;
        }

    }



