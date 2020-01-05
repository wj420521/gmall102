package cn.wj.gmall.bean;

import java.io.Serializable;
import java.util.List;

//搜索参数
public class SearchParam implements Serializable {
   //三级分类id
    private String catalog3Id;
    //关键字
    private String keyword;
    //平台属性值
    private String[] valueId;

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }
}
