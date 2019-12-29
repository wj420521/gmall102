package cn.wj.gmall.manage.controller;

import cn.wj.gmall.bean.*;
import cn.wj.gmall.service.PmsBaseService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    PmsBaseService pmsBaseService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> list = pmsBaseService.getCatalog1();
        return list;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){
        List<PmsBaseCatalog2> list = pmsBaseService.getCatalog2(catalog1Id);
        return list;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        List<PmsBaseCatalog3> list = pmsBaseService.getCatalog3(catalog2Id);
        return list;
    }
    //查询平台属性列表
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        List<PmsBaseAttrInfo> list = pmsBaseService.getAttrInfoList(catalog3Id);
        return list;
    }
    //根据平台属性id查询平台属性值列表
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){
        List<PmsBaseAttrValue> list = pmsBaseService.getAttrValueList(attrId);
        return list;
    }
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        String id = pmsBaseAttrInfo.getId();
        if(StringUtils.isNotBlank(id)){
            //属性id不为空修改
            pmsBaseService.updatePmsBaseAttr(pmsBaseAttrInfo);
        }else{
            //属性id为空时 是执行的添加操作
            pmsBaseService.addPmsBaseAttr(pmsBaseAttrInfo);

        }

        return "success";
    }


}
