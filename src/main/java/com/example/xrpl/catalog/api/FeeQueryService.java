package com.example.xrpl.catalog.api;


public interface FeeQueryService {
    FeeDto findFee(long challengerId);
}