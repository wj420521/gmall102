package cn.wj.gmall.search.service.impl;

import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.SearchParam;
import cn.wj.gmall.service.SearchService;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public List<PmsSkuInfo> getSkuInfoList(SearchParam searchParam) {
        List<PmsSkuInfo> skuInfoList = new ArrayList<>();
        String esDsl = getEsDsl(searchParam);
        Search search = new Search.Builder(esDsl).addIndex("gmall102").addType("SkuInfo").build();
        try {
            SearchResult result = jestClient.execute(search);
            List<SearchResult.Hit<PmsSkuInfo, Void>> hits = result.getHits(PmsSkuInfo.class);
            for (SearchResult.Hit<PmsSkuInfo, Void> hit : hits) {
                PmsSkuInfo skuInfo = hit.source;
                Map<String, List<String>> highlight = hit.highlight;
                //高亮的字段的值就一个 所以get(0)
               if(searchParam.getKeyword()!=null&&searchParam.getKeyword()!=""){
                   String skuName = highlight.get("skuName").get(0);
                   skuInfo.setSkuName(skuName);
               }
                skuInfoList.add(skuInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuInfoList;
    }

    //获取Dsl语句
    public String getEsDsl(SearchParam searchParam){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder bool = new BoolQueryBuilder();
        if(searchParam.getCatalog3Id()!=null){
            TermQueryBuilder term = new TermQueryBuilder("catalog3Id",searchParam.getCatalog3Id());
            bool.filter(term);
        }
        if(searchParam.getValueId()!=null){
            String valueIds[] = searchParam.getValueId();
            for (String valueId : valueIds) {
                TermQueryBuilder term = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                bool.filter(term);
            }
        }
        if(searchParam.getKeyword()!=null&&searchParam.getKeyword()!=""){
            MatchQueryBuilder match = new MatchQueryBuilder("skuName",searchParam.getKeyword());
            bool.must(match);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //给关键字加高亮
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red' >");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);
        }

        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        SearchSourceBuilder dsl = searchSourceBuilder.query(bool);
        System.out.println(dsl);
        return dsl.toString();
    }

    //向Es添加数据
    public void putEs(){
        List<PmsSkuInfo> allSkuInfo = skuService.getAllSkuInfo();
        for (PmsSkuInfo pmsSkuInfo : allSkuInfo) {
            Index index = new Index.Builder(pmsSkuInfo).index("gmall102").type("SkuInfo").id(pmsSkuInfo.getId()).build();
            try {
                jestClient.execute(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //更新热度值
    public void updateHotScore(String skuId,Long hotScore){
       String hostJson = "{\n"+
               "\"doc\""+":"+"{\n"+
               "\"hotScore\""+":"+hotScore+
               "}\n"+
               "}\n";
        Update updateHotScore = new Update.Builder(hostJson).index("gmall102").type("SkuInfo").id(skuId).build();
        try {
            jestClient.execute(updateHotScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //热度值redis计数器
    public void hotScoreCount(String skuId){
        int count=100;
        Jedis jedis = redisUtil.getJedis();
        Double hotScore = jedis.zincrby("hotScore", 1, "skuHotScore:" + skuId);
       //100次才更新一次es 降低写操作
       // if(hotScore%count==0){
            //Math.round()返回参数中最接近的 long ，其中 long四舍五入为正无穷大。
            updateHotScore(skuId,Math.round(hotScore));
       // }
        jedis.close();
    }
}
