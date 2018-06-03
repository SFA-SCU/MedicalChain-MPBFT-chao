package com.pancake.entity.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2018/6/2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record extends TxContent {
    private String id;
    private String patientId;
    private String patientName;
    private String diagnosisDate;  //诊断日期
    private String infectionName;  //传染病名称
    private String reportOrganization;  //报告单位

    public Record() {
    }

    public Record(String id, String patientId, String patientName, String diagnosisDate,
                  String infectionName, String reportOrganization) {
        super("Record");
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.diagnosisDate = diagnosisDate;
        this.infectionName = infectionName;
        this.reportOrganization = reportOrganization;
    }

    public Record(String contentType, String id, String patientId, String patientName, String diagnosisDate,
                  String infectionName, String reportOrganization) {
        super(contentType);
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.diagnosisDate = diagnosisDate;
        this.infectionName = infectionName;
        this.reportOrganization = reportOrganization;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(String diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public String getInfectionName() {
        return infectionName;
    }

    public void setInfectionName(String infectionName) {
        this.infectionName = infectionName;
    }

    public String getReportOrganization() {
        return reportOrganization;
    }

    public void setReportOrganization(String reportOrganization) {
        this.reportOrganization = reportOrganization;
    }
}
