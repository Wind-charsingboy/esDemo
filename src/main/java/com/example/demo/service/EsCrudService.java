package com.example.demo.service;

import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.List;

/**
 * @author yinhongchao
 * @date 2020/6/28 13:49
 */
public interface EsCrudService {

    /**
     * 功能描述: 创建索引
     *
     * @param indexName 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    String createIndex(String indexName) throws IOException;

    /**
     * 功能描述: 删除索引
     *
     * @param indexName 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    void deleteIndex(String indexName) throws IOException;

    /**
     * 功能描述: 查询索引
     *
     * @param indexName 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    boolean existIndex(String indexName) throws IOException;

    /**
     * 功能描述: 往索引里插入数据
     *
     * @param indexName 索引名称
     * @param docType   数据对象类型，可以把它设置成对象类的类名
     * @param object    数据对象
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    String insert(String indexName, String docType, Object object) throws IOException;

    /**
     * 功能描述: 根据索引名称和类型去查询匹配的信息
     *
     * @param indexName 索引名称
     * @param docType   数据对象类型，可以把它设置成对象类的类名
     * @return:java.util.List<java.lang.String>
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    List<String> search(String indexName, String docType) throws IOException;

    /**
     * 功能描述: 更新数据
     *
     * @param indexName 索引名称
     * @param docType   数据对象类型，可以把它设置成对象类的类名
     * @param object    数据对象
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    RestStatus update(String indexName, String docType, String id, Object object) throws IOException;

    /**
     * 功能描述: 根据id删除数据
     *
     * @param id _id编号
     * @return:org.elasticsearch.rest.RestStatus
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    RestStatus delete(String id) throws IOException;
}
