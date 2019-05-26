package cn.itcast.core.pojo.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象
 * 封装分页需要返回的数据
 */
public class PageResult implements Serializable{

    private Long total;//总记录数
    private List rows;//当前页结果
   /* private String status;//状态码

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }*/

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
