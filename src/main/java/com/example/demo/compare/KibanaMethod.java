package com.ijovo.billionbottle.search.service.elastic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.compare.ProductSearchParamDTO;
import com.example.demo.compare.ProductSearchResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yinhongchao
 * @date 2020/7/9 10:15
 */
@Service
@Slf4j
public class KibanaMethod{
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    public void searchProduct(ProductSearchParamDTO productSearchParamDTO) throws IOException {
        //构建请求
        JSONObject jsonRequestObj = new JSONObject();

        //构建分页数据
//        if (productSearchVO.getFrom() != null){
//            jsonRequestObj.put("from", productSearchVO.getFrom());
//        }
//        if (productSearchVO.getSize() != null){
//            jsonRequestObj.put("size", productSearchVO.getSize());
//        }

        //构建query
        jsonRequestObj.put("query",new JSONObject());

        //构建function score
        jsonRequestObj.getJSONObject("query").put("function_score",new JSONObject());

        //构建query
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("query",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").put("bool",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool").put("must",new JSONArray());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());

        //构建match query  queryIndex是查询的偏移量
        int queryIndex = 0;
        //只查询上架商品
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).put("term",new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("product_status",1);

        //关键字查询
        if (productSearchParamDTO.getKeyWord() != null){
            queryIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("match",new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").put("product_name",productSearchParamDTO.getKeyWord());
        }

        //构建functions部分
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("functions",new JSONArray());

        //综合排序
        if (productSearchParamDTO.getSortBy() == 0){
            sortByComprehensive(jsonRequestObj);
        }
        //按照评分需要排序
        if (productSearchParamDTO.getSortBy() == 1){
            sortByField(jsonRequestObj, "star_level");
        }
        //按照价格需要排序
        if (productSearchParamDTO.getSortBy() == 2 || productSearchParamDTO.getSortBy() == 3){
            sortByField(jsonRequestObj, "min_price");
        }
        //按照商品上架时间需要排序
        if (productSearchParamDTO.getSortBy() == 4){
            sortByField(jsonRequestObj, "take_time");
        }


        //按照计算所得score最终排序
        jsonRequestObj.put("sort",new JSONArray());
        jsonRequestObj.getJSONArray("sort").add(new JSONObject());
        jsonRequestObj.getJSONArray("sort").getJSONObject(0).put("_score",new JSONObject());
        if(productSearchParamDTO.getSortBy() == 2){
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order","asc");
        }else{
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order","desc");
        }

        //构建请求
        Request request = new Request("GET","/product/_search");
        String reqJson = jsonRequestObj.toJSONString();
        request.setJsonEntity(reqJson);
        Response response = null;
        try {
            response = restHighLevelClient.getLowLevelClient().performRequest(request);
        } catch (IOException e) {
            log.error("product search error:{}", e);
        }

        //获取返回值
        String responseStr = null;
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (response != null){
            try {
                responseStr = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                log.error("IOException:{}", e);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            JSONArray jsonArr = jsonObject.getJSONObject("hits").getJSONArray("hits");
            for(int i = 0; i < jsonArr.size(); i++){
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                Map<String, Object> resultMap = (Map<String, Object>)jsonObj.get("_source");
                resultList.add(resultMap);
            }
        }
        ProductSearchResultDTO productSearchResultDTO = new ProductSearchResultDTO();
        productSearchResultDTO.setResultList(resultList);
    }

    /**
     * 综合排序
     * @param jsonRequestObj
     */
    private void sortByComprehensive(JSONObject jsonRequestObj) {
        int functionIndex = 0;
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                .put("field", "drink_num");
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.5);

        functionIndex++;
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                .put("field", "view_num");
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.3);

        functionIndex++;
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                .put("field", "star_level");
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.2);
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "replace");
    }


    /**
     * 按照所需字段进行排序
     * @param jsonRequestObj
     * @param sortField
     */
    private void sortByField(JSONObject jsonRequestObj, String sortField) {
        int functionIndex = 0;
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                .put("field", sortField);
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "replace");
    }



}
