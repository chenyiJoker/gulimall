package com.cy.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.cy.gulimall.product.entity.AttrEntity;
import com.cy.gulimall.product.service.AttrAttrgroupRelationService;
import com.cy.gulimall.product.service.AttrService;
import com.cy.gulimall.product.service.CategoryService;
import com.cy.gulimall.product.vo.AttrGounpRelationVo;
import com.cy.gulimall.product.vo.AttrGounpWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cy.gulimall.product.entity.AttrGroupEntity;
import com.cy.gulimall.product.service.AttrGroupService;
import com.cy.common.utils.PageUtils;
import com.cy.common.utils.R;



/**
 * 属性分组
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 01:03:02
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    // 获取分类下所有分组和关联属性
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        // 1,查出当前分类下的所有属性分组
        // 2,查出每个属性分组的所有属性
        List<AttrGounpWithAttrsVo> data = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data", data);
    }
    // 添加属性与分组关联关系
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGounpRelationVo> vos) {
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    // 获取属性分组的关联的所有属性
    @GetMapping("/{attrGounpId}/attr/relation")
    public R attrRelation(@PathVariable("attrGounpId") Long attrGounpId) {
        List<AttrEntity> entities =  attrService.getRelationAttr(attrGounpId);
        return R.ok().put("data", entities);
    }

    // 获取属性分组没有关联的其他属性
    @GetMapping("/{attrGounpId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrGounpId") Long attrGounpId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrGounpId);
        return R.ok().put("page", page);
    }

    // 删除属性与分组的关联关系
    @RequestMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGounpRelationVo[] relationVo) {
        attrService.deleteRelation(relationVo);
        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getAttrGroupId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
