package com.pancake.entity.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by chao on 2018/6/2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "contentType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Record.class, name = "Record"),
        @JsonSubTypes.Type(value = TxString.class, name = "TxString")})
public class TxContent {
    private String contentType;

    public TxContent() {
    }

    public TxContent(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
