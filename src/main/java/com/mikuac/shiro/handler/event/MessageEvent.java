package com.mikuac.shiro.handler.event;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.common.utils.EventUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.GuildMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MessageEventEnum;
import com.mikuac.shiro.handler.injection.InjectionHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author zero
 */
@Component
public class MessageEvent {

    @Resource
    private EventUtils utils;

    @Resource
    private InjectionHandler injection;

    /**
     * 存储消息事件处理器
     */
    public final Map<String, BiConsumer<Bot, JSONObject>> handlers = new HashMap<>();

    /**
     * 消息事件分发
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void handler(@NotNull Bot bot, @NotNull JSONObject resp) {
        String type = resp.getString("message_type");
        handlers.getOrDefault(type, (b, e) -> {
        }).accept(bot, resp);
    }

    /**
     * 事件处理
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     * @param type {@link MessageEventEnum}
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void process(@NotNull Bot bot, JSONObject resp, MessageEventEnum type) {
        try {
            if (type == MessageEventEnum.FRIEND) {
                PrivateMessageEvent event = resp.to(PrivateMessageEvent.class);
                if (utils.setInterceptor(bot, event)) {
                    return;
                }
                event.setArrayMsg(utils.setAnyMessageEvent(bot, resp, event));
                injection.invokePrivateMessage(bot, event);
                bot.getPluginList().stream().anyMatch(o -> utils.getPlugin(o).onPrivateMessage(bot, event) == BotPlugin.MESSAGE_BLOCK);
                utils.getInterceptor(bot.getBotMessageEventInterceptor()).afterCompletion(bot, event);
            }

            if (type == MessageEventEnum.GROUP) {
                GroupMessageEvent event = resp.to(GroupMessageEvent.class);
                if (utils.setInterceptor(bot, event)) {
                    return;
                }
                event.setArrayMsg(utils.setAnyMessageEvent(bot, resp, event));
                injection.invokeGroupMessage(bot, event);
                bot.getPluginList().stream().anyMatch(o -> utils.getPlugin(o).onGroupMessage(bot, event) == BotPlugin.MESSAGE_BLOCK);
                utils.getInterceptor(bot.getBotMessageEventInterceptor()).afterCompletion(bot, event);
            }

            if (type == MessageEventEnum.GUILD) {
                GuildMessageEvent event = resp.to(GuildMessageEvent.class);
                if (utils.setInterceptor(bot, event)) {
                    return;
                }
                event.setArrayMsg(ShiroUtils.rawToArrayMsg(event.getMessage()));
                injection.invokeGuildMessage(bot, event);
                bot.getPluginList().stream().anyMatch(o -> utils.getPlugin(o).onGuildMessage(bot, event) == BotPlugin.MESSAGE_BLOCK);
                utils.getInterceptor(bot.getBotMessageEventInterceptor()).afterCompletion(bot, event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 私聊请求
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void friend(@NotNull Bot bot, @NotNull JSONObject resp) {
        process(bot, resp, MessageEventEnum.FRIEND);
    }

    /**
     * 群消息
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void group(@NotNull Bot bot, @NotNull JSONObject resp) {
        process(bot, resp, MessageEventEnum.GROUP);
    }

    /**
     * 频道消息
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void guild(@NotNull Bot bot, @NotNull JSONObject resp) {
        process(bot, resp, MessageEventEnum.GUILD);
    }

}