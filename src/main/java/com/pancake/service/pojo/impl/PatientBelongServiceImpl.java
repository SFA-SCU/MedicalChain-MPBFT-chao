package com.pancake.service.pojo.impl;

import com.pancake.dao.PatientBelongDao;
import com.pancake.entity.pojo.PatientBelong;
import com.pancake.service.pojo.PatientBelongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientBelongServiceImpl implements PatientBelongService {

    @Autowired
    private PatientBelongDao patientBelongDao;

    public List<PatientBelong> getAll() throws Exception {
        List<PatientBelong> list = patientBelongDao.queryAll();
        if (null == list) {
            throw new Exception("No patient belong was found.");
        }
        return list;
    }
}
