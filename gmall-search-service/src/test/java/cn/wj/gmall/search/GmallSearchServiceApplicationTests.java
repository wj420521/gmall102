package cn.wj.gmall.search;

import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.SearchParam;
import cn.wj.gmall.service.SkuService;
import com.alibaba.dubbo.config.annotation.Reference;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.lucene.queryparser.xml.builders.TermsQueryBuilder;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;
    @Test
    public void contextLoads() {
            putEs();
    }
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
        public void getEs(SearchParam searchParam){
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
            }
            //高亮
            searchSourceBuilder.highlight(null);
            //分页
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(20);

            SearchSourceBuilder dsl = searchSourceBuilder.query(bool);
            System.out.println(dsl);
        }
}
