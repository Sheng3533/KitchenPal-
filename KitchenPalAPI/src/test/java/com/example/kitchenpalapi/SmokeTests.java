package com.example.kitchenpalapi;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 后端冒烟测试：覆盖无 DB 依赖的核心组件。
 * JWT 签发/校验、ApiResponse 结构；足够快速、稳定，可在 CI 中运行。
 */
class SmokeTests {

    private JwtUtil newJwtUtil() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret",
                "kitchenpal-test-secret-key-0123456789-abcdefghijklmnop");
        ReflectionTestUtils.setField(util, "expiration", 3_600_000L);
        return util;
    }

    @Test
    void jwt_generateAndParse_roundTrip() {
        JwtUtil jwt = newJwtUtil();
        String token = jwt.generateToken(42L, "13800138000");
        assertNotNull(token);
        assertEquals(42L, jwt.parseUserId(token));
        assertTrue(jwt.validate(token));
    }

    @Test
    void jwt_invalidToken_returnsNull() {
        JwtUtil jwt = newJwtUtil();
        assertNull(jwt.parseUserId("not-a-token"));
        assertNull(jwt.parseUserId(""));
        assertFalse(jwt.validate("eyJhbGciOiJIUzI1NiJ9.bogus.bogus"));
    }

    @Test
    void jwt_tamperedToken_rejected() {
        JwtUtil jwt = newJwtUtil();
        String token = jwt.generateToken(7L, "13900139000");
        // 篡改签名段
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "AAAAtampered";
        assertNull(jwt.parseUserId(tampered));
    }

    @Test
    void jwt_wrongKey_rejected() {
        JwtUtil signer = newJwtUtil();
        String token = signer.generateToken(1L, "13800138000");

        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret",
                "a-completely-different-secret-key-9876543210-zyxwvutsrq");
        ReflectionTestUtils.setField(other, "expiration", 3_600_000L);
        assertNull(other.parseUserId(token));
    }

    @Test
    void apiResponse_successShape() {
        ApiResponse<List<String>> resp = ApiResponse.success(List.of("a", "b"));
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
        assertEquals(2, resp.getData().size());
    }

    @Test
    void apiResponse_customMessageAndError() {
        ApiResponse<String> ok = ApiResponse.success("操作成功", "data");
        assertEquals(200, ok.getCode());
        assertEquals("操作成功", ok.getMessage());

        ApiResponse<Void> err = ApiResponse.error("boom");
        assertEquals(500, err.getCode());
        assertEquals("boom", err.getMessage());
        assertNull(err.getData());

        ApiResponse<Void> unauthorized = ApiResponse.error(401, "未登录");
        assertEquals(401, unauthorized.getCode());
    }
}
