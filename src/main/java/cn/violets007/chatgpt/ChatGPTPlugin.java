package cn.violets007.chatgpt;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;


/**
 * @author vioelts007
 * @date: 2023/2/15 20:22
 * @description: 插件主类
 */
public class ChatGPTPlugin extends PluginBase {
    private RequestOpenAIAPI requestOpenAIAPI;
    private String usePlayerName = null;

    @Override
    public void onLoad() {
        saveResource("config.yml");
    }

    @Override
    public void onEnable() {
        getLogger().info(TextFormat.BLUE + "ChatGPTPlugin 运行");
        String apiKey = getConfig().get("apiKey").toString();
        int maxTokens = getConfig().getInt("maxTokens",2048);
        double topP = getConfig().getDouble("topP",0.2);
        double temperature = getConfig().getDouble("temperature",0.5f);
        getLogger().info(TextFormat.BLUE + "读取到的OpenAI的apiKey是: " + TextFormat.RED + TextFormat.BOLD + apiKey);
        this.requestOpenAIAPI = new RequestOpenAIAPI(apiKey, maxTokens, topP, temperature);
    }

    @Override
    public void onDisable() {
        getLogger().info(TextFormat.RED + "ChatGPTPlugin 关闭");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;
        String chat = args[0];

        if (chat == null || chat.length() < 1) {
            return false;
        }

        if (usePlayerName != null && !usePlayerName.equals("")) {
            sender.sendMessage(TextFormat.RED + "当前用户: " + TextFormat.BLUE + usePlayerName + TextFormat.RED + " 正在使用此功能,请等待");
            return true;
        }

        usePlayerName = sender.getName();
        sender.sendMessage(TextFormat.GREEN + "正在查询中....");
        getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
            @Override
            public void onRun() {
                requestOpenAIAPI.request(sender, chat);
                usePlayerName = null;
            }
        });

        return true;
    }
}
