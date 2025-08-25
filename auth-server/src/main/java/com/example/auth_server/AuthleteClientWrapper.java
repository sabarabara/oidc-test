package com.example.auth_server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class AuthleteClientWrapper {

    // シンプルな in-memory ストレージ（demo用）
    private final Map<String, String> codeToSubject = new HashMap<>(); // code -> subject
    private final Map<String, String> accessToSubject = new HashMap<>(); // access -> subject

    public String issueAuthorizationCode(String clientId, String redirectUri, String state,
                                         String codeChallenge, String codeChallengeMethod,
                                         String subject) {
        String code = "code-" + UUID.randomUUID();
        // 本来ここで codeChallenge など保存し検証する。簡易版では subject を紐づける
        codeToSubject.put(code, subject);
        return code;
    }

    public Map<String, Object> exchangeToken(String code, String redirectUri, String codeVerifier) {
        String subject = codeToSubject.remove(code);
        if (subject == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "invalid_grant");
            err.put("error_description", "invalid code");
            return err;
        }
        String access = "access-" + UUID.randomUUID();
        accessToSubject.put(access, subject);

        Map<String, Object> resp = new HashMap<>();
        resp.put("access_token", access);
        resp.put("token_type", "Bearer");
        resp.put("expires_in", 3600);
        resp.put("id_token", "idtoken-" + UUID.randomUUID()); // 実運用では JWT を返す
        return resp;
    }

    public Map<String, Object> userInfo(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Map.of("error", "invalid_request");
        }
        String token = authorizationHeader.substring(7);
        String subject = accessToSubject.get(token);
        if (subject == null) {
            return Map.of("error", "invalid_token");
        }
        // 簡易 userinfo
        Map<String, Object> u = new HashMap<>();
        u.put("sub", subject);
        u.put("preferred_username", subject);
        u.put("email", subject + "@example.com");
        return u;
    }
}
