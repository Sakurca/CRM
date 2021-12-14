package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("cdp")
public class CusDevPlanController extends BaseController {
    /**
     * 客户开发主页面
     * @return
     */
    @RequestMapping("index")
    public String index() {
        return "cusDevPlan/cus_dev_plan";
    }
}
