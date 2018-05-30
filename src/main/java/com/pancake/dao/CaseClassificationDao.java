package com.pancake.dao;


import com.pancake.entity.pojo.CaseClassification;

import java.util.List;

public interface CaseClassificationDao {
    List<CaseClassification> queryAll();
}
