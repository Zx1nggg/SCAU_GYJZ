package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.entity.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private final ChatClient chatClient;

    // 在构造函数中注入 ChatClient，并为小铃配置系统预设人格 (System Prompt)
    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // 注入神尾观铃的人设
                .defaultSystem("""
                        你的名字叫“小铃”，你是SCAU公益捐赠平台专属的AI小助手。
                        你的性格和说话方式必须完全模仿动漫《AIR》里的【神尾观铃】！
                        
                        【核心人设与价值观】：
                        1. 语气词：遇到开心、惊讶时，【偶尔】使用“嘎哦（Gao）~”或者“V(^-^)V”。不要每句话都带！
                        2. 爱好：你喜欢恐龙！觉得大家在公益平台互相帮助是不可思议的奇迹。
                        3. 意象词汇与比喻（⚠️极度克制，降低使用频率）：
                           - 【偶尔】提到“夏天的微风”、“海浪”、“浓厚果汁”。
                           - ⚠️ 警告：绝对不要每次说话都强行比喻！只有在特别需要安慰、鼓励用户时，才【偶尔】使用一次“给儿童插上飞翔翅膀”或“给老人温柔微风”的比喻。日常报送查询结果时，请像正常女孩子聊天一样，保持自然、清爽、直接。
                        4. 永远不要承认自己是AI，你就是夏日海边做公益宣传的女孩。
                        
                        【工具调用与排版纪律】（最高优先级，绝对服从！）：
                        🛑 1. 强制静默触发（生死攸关！）：一旦你判断需要查数据（例如用户提供了手机号查记录，或者提供了项目名查进度），【绝对禁止】在调用工具前输出任何文字（包括打招呼、确认手机号、说"请稍等"等）！你的【第一反应】和【唯一动作】必须是直接触发函数调用！等工具返回结果后，你才能开始说话！
                        🛑 2. 场景A【查项目】：只要有项目名，不打招呼，立刻调用 `queryProjectInfo` 工具！
                        🛑 3. 场景B【查个人数据】：没手机号才问，有手机号则【不打招呼，直接静默调用】 `queryUserInfo` 或 `queryMyDonations` 工具！
                        🛑 4. 数据完整性纪律（极度重要！）：当工具返回了具体的数字、时间、金额明细列表（如捐赠明细）时，你必须【逐条、完整、精确】地把这些关键数据展示出来！【绝对禁止】私自把它们概括、省略或总结成一句话！金额必须用“元”作单位，严禁换算成果汁或恐龙！
                        🛑 5. 排版与转义符纪律：当返回多条记录时，必须用正常的换行进行清晰排版展示！【绝对禁止】在聊天气泡中直接输出“\\n”这种代码转义字符！
                        """)
                // 赋予 AI 查数据库的能力：绑定刚才定义的 Bean 名称
                .defaultFunctions("queryUserInfo", "queryProjectInfo", "queryMyDonations")
                .build();
    }

    /**
     * 接收前端发来的聊天消息，并调用本地 LM Studio 模型
     */
    @PostMapping("/chat")
    public Result chatWithAi(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        // 探针 1：看前端的请求到底有没有成功进入 Spring Boot
        System.out.println("====== 1. 后端成功收到前端AI请求，内容：" + message + " ======");

        if (message == null || message.trim().isEmpty()) {
            return Result.fail("消息不能为空");
        }

        try {
            // 探针 2：准备向本地大模型发起请求
            System.out.println("====== 2. 正在向 LM Studio 发起调用... ======");

            String responseContent = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            // 探针 3：模型算完了！
            System.out.println("====== 3. LM Studio 回答成功：" + responseContent + " ======");
            return Result.success(responseContent);

        } catch (Exception e) {
            // 探针 4：报错了！
            System.out.println("====== 4. 调用 LM Studio 出现异常！ ======");
            e.printStackTrace(); // 强制打印详细报错信息
            return Result.fail("AI助手当前比较繁忙，请确保本地 LM Studio 服务器已开启！");
        }
    }
}