package com.pancake.web.controller;

import com.pancake.entity.util.Result;
import com.pancake.service.component.UserService;
import com.pancake.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by chao on 2017/6/13.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    /**
     * 分页返回所有用户信息
     * @return Result 对象
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    public Result queryAll() {
        // 当前页码
        int offset = 0;
        // 每页记录数
        int limit = 3;
        return ResultUtil.success(userService.getUserList(offset,limit));
    }

    /**
     * 测试
     * @return Result 对象
     */
    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public Result test() {
        return ResultUtil.success();
    }

}
