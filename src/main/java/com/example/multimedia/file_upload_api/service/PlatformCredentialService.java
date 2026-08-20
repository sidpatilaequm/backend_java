package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.entity.PlatformCredential;
import com.example.multimedia.file_upload_api.repository.PlatformCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin-editable secrets/config for external integrations (FolderIt, Microvista), stored in
 * platform_credential and looked up here instead of a hardcoded constant or an env var read
 * once at boot — so rotating a key from the admin UI takes effect on the next call, no deploy
 * or restart needed.
 *
 * get() seeds the row with the caller's fallback the first time a key is asked for (e.g.
 * FolderItService's previous hardcoded constant), so the very first call after this shipped
 * behaves exactly as before — nothing needs a manual DB migration to start working.
 */
@Service
public class PlatformCredentialService {

    private final PlatformCredentialRepository repository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public PlatformCredentialService(PlatformCredentialRepository repository) {
        this.repository = repository;
    }

    public String get(String key, String fallback) {
        String cached = cache.get(key);
        if (cached != null) return cached;

        PlatformCredential row = repository.findByCredentialKey(key).orElse(null);
        if (row == null) {
            row = new PlatformCredential();
            row.setCredentialKey(key);
            row.setCredentialValue(fallback);
            row = repository.save(row);
        }
        String value = row.getCredentialValue();
        cache.put(key, value);
        return value;
    }

    public void set(String key, String value, String updatedBy) {
        PlatformCredential row = repository.findByCredentialKey(key).orElseGet(() -> {
            PlatformCredential r = new PlatformCredential();
            r.setCredentialKey(key);
            return r;
        });
        row.setCredentialValue(value);
        row.setUpdatedBy(updatedBy);
        repository.save(row);
        cache.put(key, value);
    }
}
