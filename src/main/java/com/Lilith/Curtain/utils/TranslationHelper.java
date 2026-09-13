package com.Lilith.Curtain.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import org.apache.commons.io.IOUtils;

import com.Lilith.Curtain.CurtainRules;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TranslationHelper {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Map<String, String>> TRANS_MAP = new HashMap<String, Map<String, String>>();

    static {
        TRANS_MAP.put("zh_cn", new HashMap<String, String>());
    }

    /**
     * Player facing text. The server only sends the translation key, the client renders it in its own
     * language (assets/curtain/lang/*.lang) exactly like vanilla chat does. ChatComponentTranslation also
     * supports nested components as arguments and falls back to en_US.
     */
    public static IChatComponent translate(String key, Object... args) {
        return new ChatComponentTranslation(key, args);
    }

    /** Same as {@link #translate(String, Object...)} but with a fixed style for the client side rendering. */
    public static IChatComponent translate(String key, EnumChatFormatting formatting, ChatStyle style, Object... args) {
        IChatComponent component = new ChatComponentTranslation(key, args);
        component.setChatStyle(style == null ? new ChatStyle().setColor(formatting) : style.setColor(formatting));
        return component;
    }

    /**
     * Server side text: used where a plain string is required (item display names, console output, logs).
     * The language rule selects the table, see {@link CurtainRules#language}.
     */
    public static String translateLiteral(String key, Object... args) {
        Map<String, String> trans = TRANS_MAP.get(CurtainRules.language);
        if (trans == null) trans = new HashMap<String, String>();
        return safeFormat(trans.getOrDefault(key, key), args);
    }

    /** Server side text as a chat component, for senders that have no client (console, command blocks). */
    public static IChatComponent translateLiteralComponent(String key, Object... args) {
        return new ChatComponentText(translateLiteral(key, args));
    }

    /**
     * The text the client would show for a key, used by server side tools (RULES.MD generator) and as last
     * resort when no client translation is available.
     */
    public static String translateOnServer(String key, Object... args) {
        String translated = StatCollector.translateToLocal(key);
        if (translated.equals(key)) translated = translateLiteral(key);
        return safeFormat(translated, args);
    }

    private static String safeFormat(String raw, Object... args) {
        if (args == null || args.length == 0) return raw;
        try {
            return String.format(raw, args);
        } catch (RuntimeException e) {
            return raw;
        }
    }

    public static Collection<String> getLanguages() {
        return TRANS_MAP.keySet();
    }

    public static void addTransMap(String lang, Map<String, String> transMap) {
        if (!TRANS_MAP.containsKey(lang)) TRANS_MAP.put(lang, transMap);
        else TRANS_MAP.get(lang)
            .putAll(transMap);
    }

    public static Map<String, String> getTranslationFromResourcePath(InputStream stream) {
        String dataJSON;
        try {
            dataJSON = IOUtils.toString(stream, StandardCharsets.UTF_8);
            stream.close();
        } catch (NullPointerException | IOException e) {
            return new HashMap<String, String>();
        }
        Map<String, String> rt = GSON.fromJson(dataJSON, new TypeToken<Map<String, String>>() {}.getType());
        return rt == null ? new HashMap<String, String>() : rt;
    }
}
