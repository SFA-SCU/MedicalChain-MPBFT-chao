package com.pancake.service.pojo.impl;

import com.pancake.dao.PatientCareerDao;
import com.pancake.entity.pojo.PatientCareer;
import com.pancake.service.pojo.PatientCareerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientCareerServiceImpl implements PatientCareerService {

    @Autowired
    private PatientCareerDao patientCareerDao;

    public List<PatientCareer> getAll() throws Exception {
        List<PatientCareer> list = patientCareerDao.queryAll();
        if (null == list) {
            throw new Exception("No patient career was found.");
        }
        return list;
    }
}
