package com.omkarsathe.outvoice.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** JPA converter: persists List<Permission> as a JSON string in a TEXT column. */
@Converter
public class PermissionListConverter implements AttributeConverter<List<Permission>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize permissions list", e);
        }
    }

    @Override
    public List<Permission> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<List<Permission>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize permissions list from: " + json, e);
        }
    }
}
