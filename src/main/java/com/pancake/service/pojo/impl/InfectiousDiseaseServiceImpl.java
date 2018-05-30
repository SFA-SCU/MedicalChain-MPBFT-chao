package com.pancake.service.pojo.impl;

import com.pancake.dao.InfectiousDiseaseDao;
import com.pancake.entity.pojo.InfectiousDisease;
import com.pancake.service.pojo.InfectiousDiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfectiousDiseaseServiceImpl implements InfectiousDiseaseService {

    @Autowired
    private InfectiousDiseaseDao infectiousDiseaseDao;

    public List<InfectiousDisease> getAll() throws Exception {
        List<InfectiousDisease> list = infectiousDiseaseDao.queryAll();
        if (null == list) {
            throw new Exception("No patient career was found.");
        }
        return list;
    }
}
