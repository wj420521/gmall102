package cn.wj.gmall.manage.controller;

import cn.wj.gmall.bean.PmsProductImage;
import cn.wj.gmall.bean.PmsProductInfo;
import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> list = spuService.getSpuList(catalog3Id);
        return list;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file){
        String imgUrl = "";
        if(file!=null){
            //源文件名
            String originalFilename = file.getOriginalFilename();
            //加载配置文件
            String conf_filename = this.getClass().getResource("/tracker.conf").getFile();
            try {
                ClientGlobal.init(conf_filename);
                //创建trackerClient
                TrackerClient trackerClient = new TrackerClient();

                TrackerServer trackerServer = trackerClient.getConnection();

                StorageClient storageClient = new StorageClient(trackerServer,null);
                //扩展名
                String file_ext_name = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
                String[] strings = storageClient.upload_file(file.getBytes(),file_ext_name , null);
                 imgUrl = "http://192.168.25.142";
                for(int m=0;m<strings.length;m++){
                    String s = strings[m];
                    imgUrl+= "/"+s ;
                }
                System.out.println(imgUrl);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }
        }
        return imgUrl;
    }

    //添加spu
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String success = spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId ){
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> getSpuImageList(String spuId ){
        List<PmsProductImage> spuImageList = spuService.getSpuImageList(spuId);
        return spuImageList;
    }
}
