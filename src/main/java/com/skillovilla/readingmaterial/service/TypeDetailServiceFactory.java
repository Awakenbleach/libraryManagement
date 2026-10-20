package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.entity.MaterialType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TypeDetailServiceFactory {

    private final Map<MaterialType, TypeDetailService> serviceMap;

    public TypeDetailServiceFactory(List<TypeDetailService> services) {
        this.serviceMap = new EnumMap<>(MaterialType.class);
        for (TypeDetailService service : services) {
            serviceMap.put(service.getSupportedType(), service);
        }
    }

    public TypeDetailService getService(MaterialType type) {
        TypeDetailService service = serviceMap.get(type);
        if (service == null) {
            throw new IllegalArgumentException("No detail service registered for type: " + type);
        }
        return service;
    }
}
