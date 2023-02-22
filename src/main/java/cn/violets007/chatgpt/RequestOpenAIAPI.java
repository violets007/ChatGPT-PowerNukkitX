package cn.violets007.chatgpt;


import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author violets007
 * @date 2023/2/15
 * @description: 用于调用OpenAI的ChatGPT模型
 */
public class RequestOpenAIAPI {
    private static final String API_URL = "https://api.openai.com/v1/engines/text-davinci-003/completions";
    private String apiKey; //OpenAI的apiKey
    private int maxTokens; //限制生成的文本的长度。模型将生成不多于该数值的令牌数量的文本。默认值为64，最大值为2048。
    private double topP; //控制生成的文本的多样性。值越高，生成的文本就越多样，并且更具创意性。值越低，生成的文本就越保守，更符合逻辑。
    private double temperature; //控制生成的文本的多样性。值越高，生成的文本就越多样，并且更具创意性。值越低，生成的文本就越保守，更符合逻辑。

    public RequestOpenAIAPI(String apiKey, int maxTokens, double topP, double temperature) {
        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.temperature = temperature;
    }

    public void request(CommandSender sender, String chat) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("prompt", chat);
        paramsMap.put("temperature", temperature);
        paramsMap.put("max_tokens", maxTokens);
        paramsMap.put("top_p", topP);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        new Gson().toJson(paramsMap),
                        StandardCharsets.UTF_8
                ))
                .build();

        // Send request
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<LinkedTreeMap<String, Object>>() {
        }.getType();
        LinkedTreeMap<String, Object> choicesMap = gson.fromJson(response.body(), type);
        if (!choicesMap.containsKey("choices")) {
            if (!choicesMap.containsKey("error")) return;
            // 这里使用Gson转有点傻逼
            LinkedTreeMap<String, Object> errorMap = (LinkedTreeMap<String, Object>) choicesMap.getOrDefault("error", new LinkedTreeMap<String, Object>());
            sender.sendMessage(TextFormat.RED + "出错了: " + errorMap.get("message"));
            return;
        }

        ArrayList<LinkedTreeMap<String, Object>> choicesList = (ArrayList<LinkedTreeMap<String, Object>>) choicesMap.getOrDefault("choices", new ArrayList<>());
        LinkedTreeMap<String, Object> choiceMap = choicesList.get(0);

        String text = choiceMap.get("text").toString();
        // 移除所有的换行符
        text = text.replaceAll("\r|\n", "");
        sender.sendMessage(TextFormat.GREEN + text);
    }
}
