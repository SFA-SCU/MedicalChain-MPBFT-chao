package com.pancake.service.pojo;

import com.pancake.entity.pojo.InfectiousDisease;

import java.util.List;

public interface InfectiousDiseaseService {
    List<InfectiousDisease> getAll() throws Exception;
}
