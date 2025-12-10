package com.dipanshushukla.realtimechatappmessageservice.util;

import com.github.f4b6a3.ulid.Ulid;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UlidBinaryConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String ulidString) {
        if (ulidString == null)
            return null;

        return Ulid.from(ulidString).toBytes();
    }

    @Override
    public String convertToEntityAttribute(byte[] bytes) {
        return Ulid.from(bytes).toString();
    }
}
