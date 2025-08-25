package com.example.client_app;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.client_app.model.AuditRecord;
import com.example.client_app.repository.AuditRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Component
public class LoginFailureAuditHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private AuditRepository auditRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {   // ← 追加
        AuditRecord record = new AuditRecord();
        record.setUsername(request.getParameter("username"));
        record.setIp(request.getRemoteAddr());
        record.setTimestamp(LocalDateTime.now());
        record.setSuccess(false);
        record.setClientId("authlete-client");
        auditRepository.save(record);

        super.onAuthenticationFailure(request, response, exception);
    }
}