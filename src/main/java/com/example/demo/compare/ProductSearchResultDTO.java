package com.example.demo.compare;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author yinhongchao
 * @date 2020/7/9 9:58
 */
@Data
public class ProductSearchResultDTO {

    /**
     * 商品搜索结果集
     */
    private List<Map<String, Object>> resultList;

    /**
     * 分页信息
     */
    private PageInfoDTO pageInfoDTO;
}
