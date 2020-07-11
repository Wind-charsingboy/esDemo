package com.example.demo.compare;

import lombok.Data;

/**
 * @author yinhongchao
 * @date 2020/7/9 10:05
 */
@Data
public class ProductSearchParamDTO {
    /**
     * 搜索关键字
     */
    private String keyWord;

    /**
     * 排序字段
     */
    private Integer sortBy;

    /**
     * 起始位置
     */
    private Integer currentPage;

    /**
     * 显示条数
     */
    private Integer showCount;
}
