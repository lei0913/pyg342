package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

    PageResult findPage(TypeTemplate template, Integer page, Integer rows);

    void add(TypeTemplate template);

    void update(TypeTemplate template);

    void delete(Long[] ids);

    TypeTemplate findOne(Long id);

    List<Map> findBySpecList(Long id);
}
