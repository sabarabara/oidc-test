package com.example.client_app;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.client_app.model.AuditRecord;
import com.example.client_app.repository.AuditRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Component
public class LoginSuccessAuditHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private AuditRepository auditRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {   // ← ここを追加
        AuditRecord record = new AuditRecord();
        record.setUsername(authentication.getName());
        record.setIp(request.getRemoteAddr());
        record.setTimestamp(LocalDateTime.now());
        record.setSuccess(true);
        record.setClientId("authlete-client"); // 固定でもOK
        auditRepository.save(record);

        // 継続してデフォルト処理（/home へリダイレクトなど）
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

