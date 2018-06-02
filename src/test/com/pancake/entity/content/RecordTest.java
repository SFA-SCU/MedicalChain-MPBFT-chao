package com.pancake.entity.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by chao on 2018/6/2.
 */
public class RecordTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void string() throws Exception {
        Record record = new Record("123", "p123", "感冒");
        String str = record.toString();
        String listString = objectMapper.writeValueAsString(Arrays.asList(record));
        System.out.println(str);
        System.out.println(listString);
        TxContent txContent = objectMapper.readValue(str, Record.class);
        System.out.println(txContent);
    }

}