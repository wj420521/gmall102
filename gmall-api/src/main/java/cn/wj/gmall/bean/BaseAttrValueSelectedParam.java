package cn.wj.gmall.bean;

import java.io.Serializable;

public class BaseAttrValueSelectedParam implements Serializable {
    //属性值
    private String valueId;
    //属性名
    private String valueName;
    //url
    private String urlParam;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }
}
