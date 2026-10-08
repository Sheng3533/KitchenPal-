package com.example.kitchenpalapi.config;

import com.example.kitchenpalapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 鉴权过滤器：对受保护路径校验 Authorization: Bearer <token>。
 * - 公开只读接口（菜谱列表/详情/每日图/大厨/视频/内容/AI 助手等）无需登录，App 首页可直接用；
 * - 用户相关、写操作（注册/登录除外）、收藏/历史/通知/评论/上传等必须带有效 token，否则 401。
 * 校验通过后把 userId 放入 request attribute（"userId"），供 Controller 取用。
 */
@Component
@Order(2)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("JwtAuth");

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 完全公开（无需 token）的路径前缀
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/user/register",
            "/api/user/login",
            "/api/user/reset-password",
            "/api/recipe/list",
            "/api/recipe/hero",
            "/api/recipe/categories",
            "/api/daily-image",
            "/api/chef/list",
            "/api/chef/",
            "/api/video/list",
            "/api/content/list",
            "/api/assistant",
            "/api/knowledge",
            "/api/stats/overview",
            "/api/app/version",
            // 拍照识别食材：纯计算（识别 + 匹配菜谱），不落库、不读用户数据，
            // 与 /api/assistant 同为「无用户态的 AI 能力」，游客在首页点扫描不应 401。
            // 注意：扫描历史 /api/scan/logs 含 userId，仍受保护。
            "/api/scan/recognize",
            // 静态资源：菜谱配图 / 每日美食图 / 用户上传图，由 Coil/浏览器直接 GET，
            // 不带 Authorization 头，必须公开，否则前端全部破图（401）
            "/images",
            "/daily",
            "/uploads",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // OPTIONS 预检直接放行
        if ("OPTIONS".equals(method)) return true;
        for (String p : PUBLIC_PREFIXES) {
            if (uri.startsWith(p)) return true;
        }
        // 菜谱详情 / 评论列表：GET 公开（App 首页/详情无需登录即可看）；写操作需登录
        if (uri.startsWith("/api/recipe/") && "GET".equals(method)) return true;
        if (uri.startsWith("/api/comment/recipe/") && "GET".equals(method)) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        Long userId = null;
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            userId = jwtUtil.parseUserId(token);
        }
        if (userId == null) {
            log.warn("未授权访问被拒绝: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return;
        }
        request.setAttribute("userId", userId);
        filterChain.doFilter(request, response);
    }
}
