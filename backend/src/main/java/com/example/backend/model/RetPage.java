package com.example.backend.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RetPage<E> {
    private List<E> items;
    private long counts;
    private long pageSize;
    private long pageNum;

    public static <E> RetPage copyFromIPage(IPage<E> data) {
        return new RetPage(data.getRecords(), data.getTotal(), data.getSize(), data.getCurrent());
    }

    public static <E, T> RetPage EntityToVO(IPage<E> page, Class<T> VoClass) {
        List<E> items = page.getRecords();
        List<T> voItems = EntityToVO(items, VoClass);
        return new RetPage(voItems, page.getTotal(), page.getSize(), page.getCurrent());
    }

    public static <E, T> List<T> EntityToVO(List<E> items, Class<T> VoClass) {
        List<T> voItems = items.stream().map(i -> {
            try {
                T vo = VoClass.newInstance();
                BeanUtils.copyProperties(i, vo);
                return vo;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return voItems;
    }
}