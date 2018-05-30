package com.pancake.service.component.impl;

import com.pancake.dao.UserDao;
import com.pancake.entity.component.User;
import com.pancake.exception.UserException;
import com.pancake.service.component.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chao on 2017/6/13.
 */
@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    /**
     * 分页返回所有用户信息
     * @param offset 当前页码
     * @param limit 每页的记录数
     * @return User list
     */
    public List<User> getUserList(int offset, int limit) {
        List<User> list = userDao.queryAll(offset,limit);
        if (null == list) {
            new UserException("No user was found.");
        }
        return list;
    }
}
