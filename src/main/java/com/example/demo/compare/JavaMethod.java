package com.example.demo.compare;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class JavaMethod {
    @Autowired
    private RestHighLevelClient highLevelClient;


    public void searchProduct(ProductSearchParamDTO productSearchParamDTO) throws IOException {
        //设置查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //上架商品查询
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("product_status", 1);
        boolQueryBuilder.must(termQueryBuilder);
        //关键字查询
        if (productSearchParamDTO.getKeyWord() != null){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("product_name", productSearchParamDTO.getKeyWord());
            boolQueryBuilder.must(matchQueryBuilder);
        }


        //排序
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] scoreBuilder = null;
        if ("-1".equals(productSearchParamDTO.getSortBy())){
            scoreBuilder = sortByComprehensive();
        }else if ("1".equals(productSearchParamDTO.getSortBy())){
            scoreBuilder = sortByField("star_level");
        }else if ("2".equals(productSearchParamDTO.getSortBy()) || "3".equals(productSearchParamDTO.getSortBy())){
            scoreBuilder = sortByField("min_price");
        }else if ("4".equals(productSearchParamDTO.getSortBy())){
            scoreBuilder = sortByField("take_time");
        }
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(boolQueryBuilder, scoreBuilder)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM);
        //指定按照得分排序方式
        ScoreSortBuilder scoreSortBuilder = new ScoreSortBuilder();
        //价格升序展示 其他全部是降序展示
        if("2".equals(productSearchParamDTO.getSortBy())){
            scoreSortBuilder.order(SortOrder.ASC);
        }else{
            scoreSortBuilder.order(SortOrder.DESC);
        }


        //执行搜索
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int currentPage = productSearchParamDTO.getCurrentPage() == null ? 1 : productSearchParamDTO.getCurrentPage();
        int showCount = productSearchParamDTO.getShowCount() == null ? 10 : productSearchParamDTO.getShowCount();
        searchSourceBuilder.from(currentPage);
        searchSourceBuilder.size(showCount);
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.query(functionScoreQueryBuilder);
        searchSourceBuilder.sort(scoreSortBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        //解析查询结果
        SearchHits searchHits = searchResponse.getHits();
        List<Map<String, Object>> searchResultList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> searchResultMap = searchHit.getSourceAsMap();
            searchResultList.add(searchResultMap);
        }


        //解析分页结果
        TotalHits totalHits = searchHits.getTotalHits();
        Integer totalCount = (int) totalHits.value;
        Integer totalPage = (totalCount + showCount - 1) / showCount;
        PageInfoDTO pageInfoDTO = new PageInfoDTO();
        pageInfoDTO.setTotalCount(totalCount);
        pageInfoDTO.setCurrentPage(currentPage);
        pageInfoDTO.setTotalPage(totalPage);
        pageInfoDTO.setShowCount(showCount);


        //返回结果封装
        ProductSearchResultDTO productSearchResultDTO = new ProductSearchResultDTO();
        productSearchResultDTO.setResultList(searchResultList);
        productSearchResultDTO.setPageInfoDTO(pageInfoDTO);
    }



    /**
     * 综合排序
     * @return
     */
    private FunctionScoreQueryBuilder.FilterFunctionBuilder[] sortByComprehensive() {
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[3];
        FieldValueFactorFunctionBuilder drinkNumBuilder = new FieldValueFactorFunctionBuilder("drink_num");
        FieldValueFactorFunctionBuilder viewNumBuilder = new FieldValueFactorFunctionBuilder("view_num");
        FieldValueFactorFunctionBuilder starLevelBuilder = new FieldValueFactorFunctionBuilder("star_level");

        //设置权重
        drinkNumBuilder.setWeight(0.5f);
        viewNumBuilder.setWeight(0.3f);
        starLevelBuilder.setWeight(0.2f);
        filterFunctionBuilders[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(drinkNumBuilder);
        filterFunctionBuilders[1] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(viewNumBuilder);
        filterFunctionBuilders[2] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(starLevelBuilder);
        return filterFunctionBuilders;
    }


    /**
     * 按照所需字段进行排序
     * @param sortField
     * @return
     */
    private FunctionScoreQueryBuilder.FilterFunctionBuilder[] sortByField(String sortField) {
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[1];
        FieldValueFactorFunctionBuilder sortFieldBuilder = new FieldValueFactorFunctionBuilder(sortField);
        filterFunctionBuilders[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(sortFieldBuilder);
        return filterFunctionBuilders;
    }
}
