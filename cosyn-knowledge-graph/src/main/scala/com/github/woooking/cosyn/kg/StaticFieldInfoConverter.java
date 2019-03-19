package com.github.woooking.cosyn.kg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.woooking.cosyn.kg.entity.StaticFieldInfo;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.io.IOException;

public class StaticFieldInfoConverter implements AttributeConverter<StaticFieldInfo, String> {
    @Override
    public String toGraphProperty(StaticFieldInfo value) {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public StaticFieldInfo toEntityAttribute(String value) {
        var mapper = new ObjectMapper();
        try {
            return mapper.readValue(value, StaticFieldInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
