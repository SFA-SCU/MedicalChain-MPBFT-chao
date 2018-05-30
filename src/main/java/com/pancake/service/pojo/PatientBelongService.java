package com.pancake.service.pojo;

import com.pancake.entity.pojo.PatientBelong;

import java.util.List;

public interface PatientBelongService {
    List<PatientBelong> getAll() throws Exception;
}
