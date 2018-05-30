package com.pancake.service.pojo;

import com.pancake.entity.pojo.PatientCareer;

import java.util.List;

public interface PatientCareerService {
    List<PatientCareer> getAll() throws Exception;
}
