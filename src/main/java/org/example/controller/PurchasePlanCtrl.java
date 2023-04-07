package org.example.controller;

import org.example.pageModel.*;
import org.example.pojo.PurchasePlan;
import org.example.pojo.PurchasePlanDetail;
import org.example.service.PurchasePlanDetailService;
import org.example.service.PurchasePlanService;
import org.example.util.JsonUtils;
import org.example.webSocket.WebSocketUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购计划单 控制器
 * &#064;Author:  Szy
 * &#064;Date:  2023/3/28  14:22
 */

@RestController
@RequestMapping("/purchasePlan")
public class PurchasePlanCtrl {

    @Resource(name="purchasePlanServiceImpl")
    PurchasePlanService purchasePlanService;

    @Resource(name="purchasePlanDetailServiceImpl")
    PurchasePlanDetailService purchasePlanDetailService;

    @RequestMapping("/getData")
    public Page<PurchasePlan> getData(Pageable page, PurchasePlan purchasePlan, String start, String end){
        page.setOrderProperty("createTime");
        page.setOrderDirection(Order.Direction.desc);

        List<Filter> filters = new ArrayList<>();

        addStartAndEndRestrict(start, end, filters);
        page.setFilters(filters);

        return purchasePlanService.findPage(page,purchasePlan);
    }

    static void addStartAndEndRestrict(String start, String end, List<Filter> filters) {
        if (start!=null && start.trim().length()>0){
            Filter ft = new Filter();
            ft.setProperty("createTime");
            ft.setValue(start);
            ft.setOperator(Filter.Operator.ge);
            filters.add(ft);
        }
        if (end!=null && end.trim().length()>0){
            Filter ft = new Filter();
            ft.setProperty("createTime");
            ft.setValue(end);
            ft.setOperator(Filter.Operator.le);
            filters.add(ft);
        }
    }


    @RequestMapping("/getSinglePurchasePlan")
    public PurchasePlan getSinglePurchasePlan(Integer id){
        return purchasePlanService.find(id);
    }

    @RequestMapping("/getPurchasePlanDetails/{purchasePlanPid}")
    public Page<PurchasePlanDetail> getPurchasePlanDetails(Pageable page,@PathVariable("purchasePlanPid") Integer purchasePlanPid){

        if (purchasePlanPid!=null ){
            List<Filter> filters = new ArrayList<>();
            Filter ft = new Filter();
            ft.setProperty("purchasePlan");
            ft.setValue(purchasePlanPid);
            ft.setOperator(Filter.Operator.eq);
            filters.add(ft);
            page.setFilters(filters);
            return purchasePlanDetailService.findPage(page);
        }else
            return null;
    }

    @RequestMapping("/edit")
    public Json edit(PurchasePlan purchasePlan,
                     @RequestParam(value = "inserted", required = false) String inserted,
                     @RequestParam(value = "updated", required = false) String updated,
                     @RequestParam(value = "deleted", required = false) String deleted
                     ){
        Json json = new Json();
        List<PurchasePlanDetailDto> lstInserted = new ArrayList<>();
        List<PurchasePlanDetailDto> lstUpdated = new ArrayList<>();
        List<PurchasePlanDetailDto> lstDeleted = new ArrayList<>();


        if (!inserted.isEmpty()){
             lstInserted = JsonUtils.getListBeans(inserted, PurchasePlanDetailDto.class);
        }
        if (!updated.isEmpty()){
            lstUpdated = JsonUtils.getListBeans(updated, PurchasePlanDetailDto.class);
        }
        if (!deleted.isEmpty()){
            lstDeleted = JsonUtils.getListBeans(deleted, PurchasePlanDetailDto.class);
        }

        String errStr="";
        try {
            boolean hasEdit = purchasePlanService.editPurchasePlan(purchasePlan, lstInserted, lstUpdated, lstDeleted);
            if (hasEdit) {
                json.setSuccess(true);
                json.setMsg("采购计划单更新成功！");
              //  WebSocketUtil.sendPurchasePlanStringMsg(WebSocketUtil.generateMsg(null, 0));
                return  json;
            }
        }catch (Exception e){
           errStr = e.getMessage();
        }
        json.setSuccess(false);
        json.setMsg("采购计划单更新失败！\n\n" + errStr);
        return  json;
    }


    @RequestMapping("/add")
    public Json add(PurchasePlan purchasePlan,
                     @RequestParam(value = "inserted", required = false) String inserted,
                    @RequestParam(value = "updated", required = false) String updated){
        Json json = new Json();
        List<PurchasePlanDetailDto> lstInserted = new ArrayList<>();
        List<PurchasePlanDetailDto> lstUpdated = new ArrayList<>();

        if (!inserted.isEmpty()){
            lstInserted = JsonUtils.getListBeans(inserted, PurchasePlanDetailDto.class);
        }
        if (!updated.isEmpty()){
            lstUpdated = JsonUtils.getListBeans(updated, PurchasePlanDetailDto.class);
        }

        if (lstInserted!=null && lstUpdated!=null)
            lstInserted.addAll(lstUpdated);

        String errStr="";

        try {
            boolean hasAdded = purchasePlanService.addPurchasePlan(purchasePlan, lstInserted);
            if (hasAdded) {
                json.setSuccess(true);
                Integer id = purchasePlan.getId();
                json.setId(id);
                json.setMsg("新增采购计划单成功！");
                WebSocketUtil.sendPurchasePlanStringMsg(WebSocketUtil.generateMsg(null, 0));
                return  json;
            }
        }catch (Exception e){
            errStr = e.getMessage();
            e.printStackTrace();
        }
        json.setSuccess(false);
        json.setMsg("采购计划单更新失败！\n\n" + errStr);
        return json;
    }

}
