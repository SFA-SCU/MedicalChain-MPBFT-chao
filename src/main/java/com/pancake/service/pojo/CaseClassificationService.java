package com.pancake.service.pojo;

import com.pancake.entity.pojo.CaseClassification;

import java.util.List;

public interface CaseClassificationService {
    List<CaseClassification> getAll() throws Exception;
}
