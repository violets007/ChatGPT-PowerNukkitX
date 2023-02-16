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
    private String apiKey = "";

    public RequestOpenAIAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public void request(CommandSender sender, String chat) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("prompt", chat);
        paramsMap.put("temperature", 0.9);
        paramsMap.put("max_tokens", 500);
        paramsMap.put("top_p", 1);

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
        sender.sendMessage(TextFormat.GREEN + text);
    }
}
