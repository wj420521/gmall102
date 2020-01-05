package cn.wj.gmall.search.controller;

import cn.wj.gmall.bean.*;
import cn.wj.gmall.service.PmsBaseService;
import cn.wj.gmall.service.SearchService;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    PmsBaseService pmsBaseService;
    //首页
    @RequestMapping("index")
    public String index(){
        return "index";
    }
    @RequestMapping("list")
    public String list(SearchParam searchParam, ModelMap modelMap){
        List<PmsSkuInfo> skuInfoList = searchService.getSkuInfoList(searchParam);
        //平台属性列表  从查出来的商品中抽取 平台属性  每一个商品都有自己的catalog3Id 但会有重复的 所以放到set
        String[] valueIds = searchParam.getValueId();
        List<BaseAttrValueSelectedParam> attrValueSelectedList = new ArrayList<>();

        Set<String> valueIdSet = new HashSet();
        for (PmsSkuInfo pmsSkuInfo : skuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
            }
        //面包屑
        if(valueIdSet!=null){
            if(valueIds!=null){
                for (String id : valueIds) {
                    //如果穿进来的valueId 和数据库查出来的一个值相等 则去除 （页面上就是点击一个valueId 这个属性就消失）
                    valueIdSet.remove(id);
                    BaseAttrValueSelectedParam selectedParam = new BaseAttrValueSelectedParam();
                    PmsBaseAttrValue baseAttrValue = pmsBaseService.getValueNameByValueId(id);
                    selectedParam.setValueId(id);
                    selectedParam.setValueName(baseAttrValue.getValueName());
                    selectedParam.setUrlParam(getBreadUrlParam(searchParam,id));
                    attrValueSelectedList.add(selectedParam);
                }
            }
        }

        if(searchParam.getKeyword()!=null){
            modelMap.put("keyword",searchParam.getKeyword());
        }
        if(valueIdSet!=null){
            List<PmsBaseAttrInfo> attrList = pmsBaseService.getAttrList(valueIdSet);
            modelMap.put("attrList",attrList);
        }

        String urlParam = getUrlParam(searchParam);
        modelMap.put("urlParam",urlParam);
        modelMap.put("skuLsInfoList",skuInfoList);
        modelMap.put("attrValueSelectedList",attrValueSelectedList);

        return "list";
    }

    public String getBreadUrlParam(SearchParam searchParam,String id){
        String urlParam="";
        String keyword = searchParam.getKeyword();
        String catalog3Id = searchParam.getCatalog3Id();
        String[] valueId = searchParam.getValueId();
        if(keyword!=null){
            urlParam="keyword="+keyword+"&";
        }
        if(catalog3Id!=null){
            urlParam="catalog3Id="+catalog3Id+"&";
        }
        if(valueId!=null){
            for (String s : valueId) {
                if(!s.equals(id)){
                    urlParam+="valueId="+s+"&";
                }

            }
        }
        urlParam = urlParam.substring(0,urlParam.lastIndexOf("&"));
        return urlParam;
    }

    public String getUrlParam(SearchParam searchParam){
        String urlParam="";
        String keyword = searchParam.getKeyword();
        String catalog3Id = searchParam.getCatalog3Id();
        String[] valueId = searchParam.getValueId();
        if(keyword!=null){
            urlParam="keyword="+keyword+"&";
        }
        if(catalog3Id!=null){
            urlParam="catalog3Id="+catalog3Id+"&";
        }
        if(valueId!=null){
            for (String s : valueId) {
                urlParam+="valueId="+s+"&";
            }
        }
        return urlParam;
    }

}
