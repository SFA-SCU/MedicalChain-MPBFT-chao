package com.pancake.entity.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 用于保存传染病报告卡
 */
public class InfectiousDiseaseReportCard {
    private String cardId; // 卡片编号
    private int cardCategory; // 报告卡类别
    private String patientName; // 患者姓名
    private String citizenId; // 身份证号
    private String patientGender; // 性别
    private String patientBirthday; // 出生日期
    private String patientWorkplace; // 工作单位
    private String patientTel; // 联系电话
    private int patientBelong; // 病人属于
    private String patientPresentAddress; // 现住址
    private int patientCareer; // 患者职业
    private int caseClassificationA; // 病例分类 I
    private int caseClassificationB; // 病例分类 II
    private String onsetDate; // 发病日期
    private String diagnosisDate; // 诊断日期
    private String deathDate; // 死亡日期
    private int infectiousDisease; // 传染病
    private String revisedDiseaseName; // 订正病名
    private String refusedReason; // 退卡原因
    private String reportUnit; // 报告单位
    private String unitTel; // 联系电话
    private String reportDoctor; // 报告医生
    private String reportDate; // 填卡日期
    private String remark; // 备注

    public InfectiousDiseaseReportCard() {
    }

    public InfectiousDiseaseReportCard(String cardId, int cardCategory, String patientName, String citizenId,
                                       String patientGender, String patientBirthday, String patientWorkplace,
                                       String patientTel, int patientBelong, String patientPresentAddress,
                                       int patientCareer, int caseClassificationA, int caseClassificationB,
                                       String onsetDate, String diagnosisDate, String deathDate, int infectiousDisease,
                                       String revisedDiseaseName, String refusedReason, String reportUnit,
                                       String unitTel, String reportDoctor, String reportDate, String remark) {
        this.cardId = cardId;
        this.cardCategory = cardCategory;
        this.patientName = patientName;
        this.citizenId = citizenId;
        this.patientGender = patientGender;
        this.patientBirthday = patientBirthday;
        this.patientWorkplace = patientWorkplace;
        this.patientTel = patientTel;
        this.patientBelong = patientBelong;
        this.patientPresentAddress = patientPresentAddress;
        this.patientCareer = patientCareer;
        this.caseClassificationA = caseClassificationA;
        this.caseClassificationB = caseClassificationB;
        this.onsetDate = onsetDate;
        this.diagnosisDate = diagnosisDate;
        this.deathDate = deathDate;
        this.infectiousDisease = infectiousDisease;
        this.revisedDiseaseName = revisedDiseaseName;
        this.refusedReason = refusedReason;
        this.reportUnit = reportUnit;
        this.unitTel = unitTel;
        this.reportDoctor = reportDoctor;
        this.reportDate = reportDate;
        this.remark = remark;
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

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getCardCategory() {
        return cardCategory;
    }

    public void setCardCategory(int cardCategory) {
        this.cardCategory = cardCategory;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientBirthday() {
        return patientBirthday;
    }

    public void setPatientBirthday(String patientBirthday) {
        this.patientBirthday = patientBirthday;
    }

    public String getPatientWorkplace() {
        return patientWorkplace;
    }

    public void setPatientWorkplace(String patientWorkplace) {
        this.patientWorkplace = patientWorkplace;
    }

    public String getPatientTel() {
        return patientTel;
    }

    public void setPatientTel(String patientTel) {
        this.patientTel = patientTel;
    }

    public int getPatientBelong() {
        return patientBelong;
    }

    public void setPatientBelong(int patientBelong) {
        this.patientBelong = patientBelong;
    }

    public String getPatientPresentAddress() {
        return patientPresentAddress;
    }

    public void setPatientPresentAddress(String patientPresentAddress) {
        this.patientPresentAddress = patientPresentAddress;
    }

    public int getPatientCareer() {
        return patientCareer;
    }

    public void setPatientCareer(int patientCareer) {
        this.patientCareer = patientCareer;
    }

    public int getCaseClassificationA() {
        return caseClassificationA;
    }

    public void setCaseClassificationA(int caseClassificationA) {
        this.caseClassificationA = caseClassificationA;
    }

    public int getCaseClassificationB() {
        return caseClassificationB;
    }

    public void setCaseClassificationB(int caseClassificationB) {
        this.caseClassificationB = caseClassificationB;
    }

    public String getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(String onsetDate) {
        this.onsetDate = onsetDate;
    }

    public String getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(String diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }

    public int getInfectiousDisease() {
        return infectiousDisease;
    }

    public void setInfectiousDisease(int infectiousDisease) {
        this.infectiousDisease = infectiousDisease;
    }

    public String getRevisedDiseaseName() {
        return revisedDiseaseName;
    }

    public void setRevisedDiseaseName(String revisedDiseaseName) {
        this.revisedDiseaseName = revisedDiseaseName;
    }

    public String getRefusedReason() {
        return refusedReason;
    }

    public void setRefusedReason(String refusedReason) {
        this.refusedReason = refusedReason;
    }

    public String getReportUnit() {
        return reportUnit;
    }

    public void setReportUnit(String reportUnit) {
        this.reportUnit = reportUnit;
    }

    public String getUnitTel() {
        return unitTel;
    }

    public void setUnitTel(String unitTel) {
        this.unitTel = unitTel;
    }

    public String getReportDoctor() {
        return reportDoctor;
    }

    public void setReportDoctor(String reportDoctor) {
        this.reportDoctor = reportDoctor;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
