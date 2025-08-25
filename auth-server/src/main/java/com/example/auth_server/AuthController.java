package com.example.auth_server;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.model.User;
import com.example.auth_server.repository.UserRepository;
import com.example.auth_server.model.AuditRecord;
import com.example.auth_server.repository.AuditRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuthleteClientWrapper authleteClient;

    // 1) /auth/authorize - クライアントが最初に来るエンドポイント
    @GetMapping("/authorize")
    public void authorize(@RequestParam Map<String, String> params,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {

        // 簡易ログイン判定（セッションに "user" があるか）
        Object loggedUser = request.getSession().getAttribute("user");
        if (loggedUser == null) {
            // 未ログイン → login ページへパラメータそのままでリダイレクト
            String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
            response.sendRedirect("/auth/login?" + query);
            return;
        }

        // ログイン済み → 認可コード発行（authleteClient は下で示す In-memory 実装）
        String subject = loggedUser.toString();
        String code = authleteClient.issueAuthorizationCode(
                params.get("client_id"),
                params.get("redirect_uri"),
                params.get("state"),
                params.get("code_challenge"),
                params.get("code_challenge_method"),
                subject
        );

        // redirect back to client
        String redirect = params.get("redirect_uri") + "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(params.getOrDefault("state", ""), StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    // 2) GET /auth/login - ログインフォーム表示
    @GetMapping("/login")
    public String loginPage(@RequestParam Map<String, String> params, Model model) {
        // そのまま params を渡す（テンプレートは params['client_id'] 等で参照）
        model.addAttribute("params", params);
        return "login";
    }

    // 3) POST /auth/login - ログイン処理（認可サーバ側でのユーザー認証）
    @PostMapping("/login")
    public void login(@RequestParam Map<String, String> params,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {

        Optional<User> user = userRepository.findByUsername(params.get("username"));
        boolean success = user.isPresent() && user.get().getPassword().equals(params.get("password"));

        // 監査ログ
        AuditRecord record = new AuditRecord();
        record.setUsername(params.get("username"));
        record.setIp(request.getRemoteAddr());
        record.setTimestamp(LocalDateTime.now());
        record.setSuccess(success);
        record.setClientId(params.get("client_id"));
        auditRepository.save(record);

        if (!success) {
            // 認証失敗 → login ページに戻す（簡易）
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }

        // 認証成功 → セッションにユーザーを保存してから /auth/authorize を再呼び出しする
        request.getSession().setAttribute("user", params.get("username"));

        // リダイレクトして authorize の処理に戻す（params を付与）
        String query = params.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        response.sendRedirect("/auth/authorize?" + query);
    }

    // 4) POST /auth/token - 認可コードをアクセストークンに交換するエンドポイント
    @PostMapping("/token")
    @ResponseBody
    public Map<String, Object> token(@RequestParam Map<String, String> params) {
        return authleteClient.exchangeToken(
                params.get("code"),
                params.get("redirect_uri"),
                params.get("code_verifier")
        );
    }

    // 5) GET /auth/userinfo - シンプルな userinfo（クライアントが呼ぶ）
    @GetMapping("/userinfo")
    @ResponseBody
    public Map<String, Object> userinfo(@RequestHeader("Authorization") String authorization) {
        return authleteClient.userInfo(authorization);
    }
}
