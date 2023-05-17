package com.cy.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.gulimall.product.service.CategoryBrandRelationService;
import com.cy.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.Query;

import com.cy.gulimall.product.dao.CategoryDao;
import com.cy.gulimall.product.entity.CategoryEntity;
import com.cy.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1,查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2,封装成树形结构

        // 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))
        ).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单，是否被其它地方调用

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @CacheEvict(value = "catagory", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (StringUtils.hasText(category.getName())) {
            // 同步更新其他关联表的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO 更新其他关联
        }
    }

    @Cacheable(value = "catagory", key = "#root.methodName")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryEntity::getParentCid, 0);
        return baseMapper.selectList(queryWrapper);
    }

    @Cacheable(value = "catagory", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);


        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);

        Map<String, List<Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> entities = getCategoryEntities(selectList, l2.getCatId());
                    if (entities != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = entities.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return map;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");

        if (StringUtils.isEmpty(catalogJson)) {
            return getCatalogJsonFromDbWithRedissonLock();
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("catalogJson-lock");

        RLock rLock = readWriteLock.readLock();

        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            rLock.lock();
            //加锁成功...执行业务
            dataFromDb = getCatalogJsonFromDb();
        } finally {
            rLock.unlock();
        }
        return dataFromDb;

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);


        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);

        Map<String, List<Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> entities = getCategoryEntities(selectList, l2.getCatId());
                    if (entities != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = entities.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        // 将查到的数据放入缓存,将对象转为json
        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJson", s);
        return map;
    }

    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList, Long parentCid) {
//        return baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
//                .eq(CategoryEntity::getParentCid, v.getCatId()));
        return selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 收集当前节点
        paths.add(catelogId);
        if (this.getById(catelogId).getParentCid() != 0) {
            findParentPath(this.getById(catelogId).getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid().equals(root.getCatId())
        ).map(categoryEntity -> {
            // 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, entities));
            return categoryEntity;
            // 菜单的排序
        }).sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))
        ).collect(Collectors.toList());
        return children;
    }

}