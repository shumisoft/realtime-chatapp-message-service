package com.dipanshushukla.realtimechatappmessageservice.service;

import org.springframework.stereotype.Service;

import com.github.f4b6a3.ulid.UlidCreator;

@Service
public class ULIDService {

    // public String newId() {
    // return UlidCreator.getUlid().toString();
    // }

    public byte[] newId() {
        return UlidCreator.getUlid().toBytes();
    }

    public String newIdString() {
        return UlidCreator.getUlid().toString();
    }

}
