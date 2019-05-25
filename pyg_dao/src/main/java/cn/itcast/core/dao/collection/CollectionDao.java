package cn.itcast.core.dao.collection;

import cn.itcast.core.pojo.collection.Collection;
import cn.itcast.core.pojo.collection.CollectionQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CollectionDao {
    int countByExample(CollectionQuery example);

    int deleteByExample(CollectionQuery example);

    int insert(Collection record);

    int insertSelective(Collection record);

    List<Collection> selectByExample(CollectionQuery example);

    int updateByExampleSelective(@Param("record") Collection record, @Param("example") CollectionQuery example);

    int updateByExample(@Param("record") Collection record, @Param("example") CollectionQuery example);
}