package com.example.demo.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author yinhongchao
 * @date 2020/6/28 14:22
 */
@Data
public class EsDto {
    private String indexName;

    private String docType;

    private Object object;

    private String id;
}
