package com.example.xrpl.challenge.domain.converter;

import com.example.xrpl.challenge.domain.model.ProofFrequency;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProofFrequencyConverter implements AttributeConverter<ProofFrequency, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProofFrequency attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getTimes();
    }

    @Override
    public ProofFrequency convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ProofFrequency.fromTimes(dbData);
    }
}
