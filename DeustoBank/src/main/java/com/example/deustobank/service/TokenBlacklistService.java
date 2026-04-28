package com.example.deustobank.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            blacklist.add(token);
        }
    }

    public boolean isInvalidated(String token) {
        return token != null && blacklist.contains(token);
    }
}
