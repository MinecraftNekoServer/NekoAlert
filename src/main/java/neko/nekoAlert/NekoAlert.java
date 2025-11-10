package neko.nekoAlert;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

import java.io.IOException;

import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.concurrent.TimeUnit;

@Plugin(id = "nekoalert", name = "NekoAlert", version = "1.0-SNAPSHOT", url = "https://cnmsb.xin/", authors = {"不穿胖次の小奶猫"})
public class NekoAlert {

    @Inject
    private Logger logger;
    
    @Inject
    private ProxyServer server;
    
    private Map<String, List<String>> messages;

    private boolean lineSent = false; // 标记当前行是否已发送

    private Path configPath;

    private String currentLine = null;

    private int globalInterval = 60; // 全局间隔时间

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        startAlertTask();
        
        // 注册命令
        server.getCommandManager().register("nekoalert", new ReloadCommand(), "na");
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // 插件关闭时的清理工作
    }
    
    private void loadConfig() {
        try {
            // 获取插件配置目录
            configPath = Path.of("").toAbsolutePath().resolve("plugins").resolve("NekoAlert");
            Files.createDirectories(configPath);
            
            // 加载配置文件
            Path configFile = configPath.resolve("config.yml");
            if (!Files.exists(configFile)) {
                // 如果配置文件不存在，从资源目录复制默认配置文件
                try (InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (defaultConfigStream != null) {
                        Files.copy(defaultConfigStream, configFile);
                    } else {
                        // 如果jar中也没有配置文件，则创建一个默认的
                        String defaultConfig = "time: 60\n" +
                                "messages:\n" +
                                "  line1:\n" +
                                "    - ''\n" +
                                "  line2:\n" +
                                "    - ''\n";
                        Files.write(configFile, defaultConfig.getBytes());
                    }
                } catch (IOException e) {
                    logger.error("创建默认配置文件时出错", e);
                    // 如果创建失败，创建一个空的配置结构
                    messages = new HashMap<>();
                    List<String> emptyList = new ArrayList<>();
                    messages.put("line1", emptyList);
                    return;
                }
            }
            
            // 读取配置文件内容
            Yaml yaml = new Yaml();
            String configContent = Files.readString(configFile);
            Map<String, Object> config = yaml.load(configContent);
            
            // 读取全局时间设置
            Integer globalTime = (Integer) config.get("time");
            globalInterval = globalTime != null ? globalTime : 60;
            
            // 转换配置数据结构
            messages = (Map<String, List<String>>) config.get("messages");
            
            // 初始化当前行
            if (currentLine == null && messages != null && !messages.isEmpty()) {
                currentLine = messages.keySet().iterator().next();
            }
            
            logger.info("配置文件加载成功，加载了 {} 条消息, 间隔时间 {}秒", messages != null ? messages.size() : 0, globalInterval);
        } catch (IOException e) {
            logger.error("加载配置文件时出错", e);
        }
    }
    
    private void startAlertTask() {
        // 启动一个初始任务，后续会根据消息间隔时间动态调整
        scheduleNextTask();
    }
    
    private void scheduleNextTask() {
        // 检查当前行是否有效
        if (messages == null || messages.isEmpty()) {
            // 如果没有消息，1秒后重试
            globalInterval = 1;
        } else {
            // 如果当前行为空或不存在，设置为第一行
            if (currentLine == null || !messages.containsKey(currentLine)) {
                currentLine = messages.keySet().iterator().next();
                lineSent = false; // 重置行发送状态
            }
            
            List<String> currentMessages = messages.get(currentLine);
            if (currentMessages != null && !currentMessages.isEmpty()) {
                // 如果当前行还没有发送，则发送整行的所有消息
                if (!lineSent) {
                    // 发送当前行的所有消息
                    for (String message : currentMessages) {
                        // 使用MiniMessage解析颜色代码和样式

                        String formattedMessage = message;

                        formattedMessage = formattedMessage.replace("&0", "<black>")

                            .replace("&1", "<dark_blue>")

                            .replace("&2", "<dark_green>")

                            .replace("&3", "<dark_aqua>")

                            .replace("&4", "<dark_red>")

                            .replace("&5", "<dark_purple>")

                            .replace("&6", "<gold>")

                            .replace("&7", "<gray>")

                            .replace("&8", "<dark_gray>")

                            .replace("&9", "<blue>")

                            .replace("&a", "<green>")

                            .replace("&b", "<aqua>")

                            .replace("&c", "<red>")

                            .replace("&d", "<light_purple>")

                            .replace("&e", "<yellow>")

                            .replace("&f", "<white>")

                            .replace("&k", "<obfuscated>")

                            .replace("&l", "<bold>")

                            .replace("&m", "<strikethrough>")

                            .replace("&n", "<underlined>")

                            .replace("&o", "<italic>")

                            .replace("&r", "<reset>");

                        

                        // 使用MiniMessage解析格式化消息

                        Component component = MiniMessage.miniMessage().deserialize(formattedMessage);
                        // 发送消息给所有在线玩家
                        server.getAllPlayers().forEach(player -> player.sendMessage(component));
                    }
                    
                    // 标记当前行已发送
                    lineSent = true;
                }
                
                // 如果当前行已发送，切换到下一行
                if (lineSent) {
                    switchToNextLine();
                    lineSent = false; // 重置下一行的发送状态
                }
            } else {
                // 如果当前行没有消息，设置默认间隔时间
                globalInterval = 60;
            }
        }
        
        // 使用全局间隔时间调度下一次任务
        server.getScheduler()
            .buildTask(this, this::scheduleNextTask)
            .delay(globalInterval, TimeUnit.SECONDS)
            .schedule();
    }
    
    private void switchToNextLine() {
        // 获取所有行的键
        List<String> lineKeys = new ArrayList<>(messages.keySet());
        
        if (!lineKeys.isEmpty()) {
            // 找到当前行的索引
            int currentLineIndex = lineKeys.indexOf(currentLine);
            // 计算下一行的索引（循环）
            int nextIndex = (currentLineIndex + 1) % lineKeys.size();
            // 设置为下一行
            currentLine = lineKeys.get(nextIndex);
        }
    }
    
    // 重载配置文件的命令
    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();
            
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                loadConfig();
                source.sendMessage(Component.text("配置文件重载成功！"));
            } else {
                source.sendMessage(Component.text("使用方法: /nekoalert reload"));
            }
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            CommandSource source = invocation.source();
            return source.hasPermission("nekoalert.reload") || source instanceof ConsoleCommandSource;
        }
    }
}
