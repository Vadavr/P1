package com.java.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEnvelope {

    @NonNull
    private BigInteger userId;
    @NonNull
    private String payload;
    @NonNull
    private Timestamp timestamp;
}
