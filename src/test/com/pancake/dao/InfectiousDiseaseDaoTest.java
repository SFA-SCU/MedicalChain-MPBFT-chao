package com.pancake.dao;

import com.pancake.entity.pojo.InfectiousDisease;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-*.xml")
public class InfectiousDiseaseDaoTest {

    @Autowired
    private InfectiousDiseaseDao infectiousDiseaseDao;

    @Test
    public void queryAll() {
        List<InfectiousDisease> list = infectiousDiseaseDao.queryAll();
        for(InfectiousDisease infectiousDisease : list) {
            System.out.println(infectiousDisease);
        }
    }
}