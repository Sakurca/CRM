package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.SaleChance;
import com.yjxxt.crm.query.SaleChanceQuery;
import com.yjxxt.crm.service.SaleChanceService;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("sale_chance")
public class SaleChanceController extends BaseController {

    @Autowired
    private SaleChanceService saleChanceService;

    @Autowired
    private UserService userService;

    @RequestMapping("addOrUpdateDialog")
    public String addOrUpdate(Integer id, Model model){
        //判断
        if(id != null){
            //查询用户信息
            SaleChance saleChance = saleChanceService.selectByPrimaryKey(id);
            //存储
            model.addAttribute("saleChance",saleChance);
        }
        return "saleChance/add_update";
    }

    @RequestMapping("index")
    public String index(){
        return "saleChance/sale_chance";
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> saylist(SaleChanceQuery saleChanceQuery){
        //调用方法获取数据
        Map<String, Object> map = saleChanceService.querySaleChanceByParams(saleChanceQuery);
        //map---json
        //返回map
        return map;
    }

    @RequestMapping("save")
    @ResponseBody
    public ResultInfo save(HttpServletRequest req,SaleChance saleChance){
        //获取用户id
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        String trueName = userService.selectByPrimaryKey(userId).getTrueName();
        //创建人
        saleChance.setCreateMan(trueName);
        //添加操作
        saleChanceService.addSaleChance(saleChance);
        //返回目标对象
        return success("添加成功！");
    }

    @RequestMapping("update")
    @ResponseBody
    public ResultInfo update(SaleChance saleChance){
        //添加操作
        saleChanceService.changeSaleChance(saleChance);
        //返回目标对象
        return success("修改成功！");
    }

    @RequestMapping("dels")
    @ResponseBody
    public ResultInfo deletes(Integer [] ids){
        //添加操作
        saleChanceService.removeSaleChanceIds(ids);
        //返回目标对象
        return success("批量删除成功！");
    }

    /**
     * 多条件查询营销机会
     * @param query
     * @param flag
     * @param request
     * @return
     */
   /* @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> QuerySaleChanceByParams(SaleChanceQuery query,Integer flag,HttpServletRequest request){
        // 查询参数 flag=1 代表当前查询为开发计划数据，设置查询分配人参数
        if(flag !=null && flag == 1){
            //获取当前登录用户的id
            int userId = LoginUserUtil.releaseUserIdFromCookie(request);
            query.setAssignMan(userId);
        }
        return saleChanceService.querySaleChanceByParams(query);
    }*/

}
