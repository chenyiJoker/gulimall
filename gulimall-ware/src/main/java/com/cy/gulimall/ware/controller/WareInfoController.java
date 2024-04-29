package com.cy.gulimall.ware.controller;

import com.cy.common.utils.PageUtils;
import com.cy.common.utils.R;
import com.cy.gulimall.ware.entity.WareInfoEntity;
import com.cy.gulimall.ware.service.WareInfoService;
import com.cy.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 仓库信息
 *
 * @author chenyi
 * @email cy1585970941@gmail.com
 * @date 2023-04-22 11:59:39
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId) {
        FareVo fareVo = wareInfoService.getFare(addrId);
        return R.ok().setData(fareVo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
