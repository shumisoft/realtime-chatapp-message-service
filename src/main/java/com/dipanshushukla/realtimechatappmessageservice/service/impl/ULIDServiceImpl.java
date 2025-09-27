package com.dipanshushukla.realtimechatappmessageservice.service.impl;

import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;
import com.github.f4b6a3.ulid.UlidCreator;

@Service
public class ULIDServiceImpl implements ULIDService {

    @Override
    public byte[] newId() {
        return UlidCreator.getUlid().toBytes();
    }

    @Override
    public String newIdString() {
        return UlidCreator.getUlid().toString();
    }

}
