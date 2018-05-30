package com.pancake.service.pojo.impl;

import com.pancake.dao.CaseClassificationDao;
import com.pancake.entity.pojo.CaseClassification;
import com.pancake.service.pojo.CaseClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaseClassificationServiceImpl implements CaseClassificationService {

    @Autowired
    private CaseClassificationDao caseClassificationDao;

    public List<CaseClassification> getAll() throws Exception {
        List<CaseClassification> list = caseClassificationDao.queryAll();
        if (null == list) {
            throw new Exception("No case classification was found.");
        }
        return list;
    }
}
