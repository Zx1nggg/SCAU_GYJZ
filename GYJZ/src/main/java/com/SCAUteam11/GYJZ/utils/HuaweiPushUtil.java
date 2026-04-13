package com.SCAUteam11.GYJZ.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class HuaweiPushUtil {

    // 开发阶段占位符：真机联调时再去华为 AppGallery Connect 后台申请
    private static final String APP_ID = "mock_app_id";
    private static final String APP_SECRET = "mock_app_secret";
    private static final String TOKEN_URL = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
    private static final String PUSH_URL = "https://push-api.cloud.huawei.com/v1/" + APP_ID + "/messages:send";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 向指定的鸿蒙设备发送 Push 通知
     *
     * @param pushTokens 目标设备的 Token 列表（从 user 表的 push_token 字段获取）
     * @param title      通知标题
     * @param body       通知正文内容
     */
    public void sendPushMessage(List<String> pushTokens, String title, String body) {
        // 如果没人订阅，或者都没绑定 Token，就直接返回，不浪费资源
        if (pushTokens == null || pushTokens.isEmpty()) {
            System.out.println("⚠️ [推送跳过] 目标 Token 列表为空，没有可推送的设备。");
            return;
        }

        try {
            // 开发调试阶段：直接在控制台打印出来，假装发给了华为服务器
            System.out.println("\n=======================================================");
            System.out.println("🚀 [华为 Push Kit 模拟发送]");
            System.out.println("📢 通知标题 : " + title);
            System.out.println("📄 通知内容 : " + body);
            System.out.println("📱 接收设备 : 共 " + pushTokens.size() + " 台");
            System.out.println("🔗 目标Token : " + pushTokens);
            System.out.println("=======================================================\n");

            // ----------------------------------------------------------------
            // 🛑 下方是真实的生产环境推送逻辑，上线前取消注释即可
            // ----------------------------------------------------------------
            /*
            // 1. 获取华为服务端 Access Token (通过 APP_ID 和 APP_SECRET 换取)
            String accessToken = getHuaweiAccessToken();

            // 2. 构建推送报文 JSON (遵循 Huawei Push Kit HTTP API 规范)
            String payload = buildPushPayload(pushTokens, title, body);

            // 3. 发送 HTTP POST 请求给华为服务器
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(PUSH_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [真实推送成功] 华为服务器已接收");
            } else {
                System.err.println("❌ [真实推送失败] 华为服务器返回: " + response.getBody());
            }
            */

        } catch (Exception e) {
            System.err.println("❌ [推送模块异常] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 模拟获取 Access Token
     */
    private String getHuaweiAccessToken() {
        // 实际开发中，这里需要发起 POST 请求到 TOKEN_URL 获取 token
        return "mock_access_token_123456";
    }

    /**
     * 模拟构建 Push Payload
     */
    private String buildPushPayload(List<String> tokens, String title, String body) {
        // 实际开发中，这里需要拼接复杂的 JSON，包含 message -> notification -> title/body 等层级
        return "{\"mock\": \"payload\"}";
    }
}