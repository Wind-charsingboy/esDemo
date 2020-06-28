package com.example.demo.controller;

import com.example.demo.dto.EsDto;
import com.example.demo.service.EsCrudService;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author yinhongchao
 * @date 2020/6/28 13:59
 */
@RequestMapping("/es")
@RestController
public class EsCrudController {
    @Autowired
    private EsCrudService esCrudService;

    /**
     * 功能描述: 创建索引
     *
     * @param esDto 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/createIndex")
    public String createIndex(@RequestBody EsDto esDto) throws IOException {
      return esCrudService.createIndex(esDto.getIndexName());
    }

    /**
     * 功能描述: 删除索引
     *
     * @param esDto 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/deleteIndex")
    public void deleteIndex(@RequestBody EsDto esDto) throws IOException {
        esCrudService.deleteIndex(esDto.getIndexName());
    }

    /**
     * 功能描述: 查询索引
     *
     * @param esDto 索引名称
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/existIndex")
    public boolean existIndex(@RequestBody EsDto esDto) throws IOException {
        return esCrudService.existIndex(esDto.getIndexName());
    }

    /**
     * 功能描述: 往索引里插入数据
     *
     * @param esDto 索引名称
     * @param esDto   数据对象类型，可以把它设置成对象类的类名
     * @param esDto    数据对象
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/insert")
    public String insert(@RequestBody EsDto esDto) throws IOException {
        return esCrudService.insert(esDto.getIndexName(), esDto.getDocType(), esDto.getObject());
    }

    /**
     * 功能描述: 根据索引名称和类型去查询匹配的信息
     *
     * @param esDto 索引名称
     * @param esDto   数据对象类型，可以把它设置成对象类的类名
     * @return:java.util.List<java.lang.String>
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/search")
    public List<String> search(@RequestBody EsDto esDto) throws IOException {
        return esCrudService.search(esDto.getIndexName(), esDto.getDocType());
    }

    /**
     * 功能描述: 更新数据
     *
     * @param esDto 索引名称
     * @param esDto   数据对象类型，可以把它设置成对象类的类名
     * @param esDto    数据对象
     * @return:void
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/update")
    public RestStatus update(@RequestBody EsDto esDto) throws IOException {
        return esCrudService.update(esDto.getIndexName(), esDto.getDocType(), esDto.getId(), esDto.getObject());
    }

    /**
     * 功能描述: 根据id删除数据
     *
     * @param esDto _id编号
     * @return:org.elasticsearch.rest.RestStatus
     * @since: v1.0
     * @author yinhongchao
     * @date 2020/6/28 13:50
     */
    @PostMapping("/delete")
    public RestStatus delete(@RequestBody EsDto esDto) throws IOException {
        return esCrudService.delete(esDto.getId());
    }

}
