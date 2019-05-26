package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;

    @Autowired
    private SpecificationOptionDao optionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult findPage(TypeTemplate template, Integer page, Integer rows) {
        /**
         * 缓存模板中的数据到redis中, 供前台搜索使用
         */
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        if (typeTemplates != null) {
            for (TypeTemplate typeTemplate : typeTemplates) {
                //1.获取品牌json字符串
                String brandJsonStr = typeTemplate.getBrandIds();
                //2. 将品牌json字符串转换成集合
                List<Map> brandList = JSON.parseArray(brandJsonStr, Map.class);
                //3. 将模板id作为小key, 品牌集合作为value缓存入redis中
                redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).put(typeTemplate.getId(), brandList);

                //4. 根据模板id, 获取规格以及规格选项集合数据
                List<Map> specList = findBySpecList(typeTemplate.getId());
                redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).put(typeTemplate.getId(), specList);
            }
        }

        /**
         * 分页查询
         */
        PageHelper.startPage(page, rows );
        //创建查询对象
        TypeTemplateQuery query = new TypeTemplateQuery();
        //创建sql语句的where查询条件对象
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        if (template != null) {
            if (template.getName() != null && !"".equals(template.getName())) {
                criteria.andNameLike("%"+template.getName()+"%");
            }
        }

        Page<TypeTemplate> templateList = (Page<TypeTemplate>)typeTemplateDao.selectByExample(query);
        return new PageResult(templateList.getTotal(), templateList.getResult());
    }

    @Override
    public void add(TypeTemplate template) {

        if ("".equals(template.getStatus())) {
            template.setStatus("0");
        }
        typeTemplateDao.insertSelective(template);
    }

    @Override
    public void update(TypeTemplate template) {
        if (!typeTemplateDao.selectByPrimaryKey(template.getId()).equals(template)){
            template.setStatus("0");

            typeTemplateDao.updateByPrimaryKeySelective(template);
        }
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
           /* for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }*/
            TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
            typeTemplateQuery.createCriteria().andIdIn(Arrays.asList(ids));
            typeTemplateDao.deleteByExample(typeTemplateQuery);
        }

    }

    /**
     * 根据模板id, 查询对应的规格集合和规格选项集合数据
     * @param id    模板id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        //1. 根据模板id, 查询对应模板实体对象
        TypeTemplate template = typeTemplateDao.selectByPrimaryKey(id);
        //2. 找到模板实体对象后获取规格json格式字符串
        String specJsonStr = template.getSpecIds();
        //3. 解析json格式字符串为java对象
        List<Map> specList = JSON.parseArray(specJsonStr, Map.class);
        //4. 遍历规格集合
        if (specList != null) {
            for (Map specMap : specList) {
                //5. 根据规格对象中的规格id, 查询对应的规格选项集合
                Long specId = Long.parseLong(String.valueOf(specMap.get("id")));
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                List<SpecificationOption> optionList = optionDao.selectByExample(query);
                //6. 将规格选项集合封装到规格对象中返回
                specMap.put("options", optionList);
            }
        }

        return specList;
    }

    //搜索
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //分页小助手
        PageHelper.startPage(page, rows);

        //条件查询
        TypeTemplateQuery query = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        //根据查询返回的审核状态确定是否显示

        //判断状态
        if (null != typeTemplate.getStatus() && !"".equals(typeTemplate.getStatus())) {
            criteria.andStatusEqualTo(typeTemplate.getStatus());
        }

        //规格名称  模糊查询
        if (null != typeTemplate.getName() && !"".equals(typeTemplate.getName().trim())) {
            criteria.andNameLike("%" + typeTemplate.getName().trim() + "%");
        }


        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }

    //查询所有
    @Override
    public List<TypeTemplate> findAll() {
        List<TypeTemplate> templateList = typeTemplateDao.selectByExample(null);
        return templateList;
    }

    //开始审核  参数1:数组 商品表 的ID    参数2： 驳回  2
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
            typeTemplate.setStatus(status);
            typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);

        }
    }

}
