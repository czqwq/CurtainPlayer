package com.Lilith.Curtain.api.rules;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.MinecraftServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RuleManager {

    public static final List<Class<?>> LIMIT = new ArrayList<Class<?>>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();
    public static final Map<String, CurtainRule<?>> RULES = new HashMap<String, CurtainRule<?>>();
    public static final Map<String, List<String>> CATEGORIES_RULES = new HashMap<String, List<String>>();
    /** Every rule known to this manager, in declaration order. */
    public final Map<String, CurtainRule<?>> ruleMap = new HashMap<String, CurtainRule<?>>();
    /** Rules that are explicitly persisted to disk (set as default by an operator). */
    public final Map<String, CurtainRule<?>> defaultRuleMap = new HashMap<String, CurtainRule<?>>();
    private final MinecraftServer server;
    private final String id;

    static {
        Collections.addAll(
            LIMIT,
            String.class,
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            boolean.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class);
    }

    public RuleManager(MinecraftServer server, String id) {
        this.server = server;
        this.id = id;
        RULES.forEach((s, r) -> r.reset());
        this.loadFromFile();
        this.ruleMap.putAll(RULES);
    }

    public static void addRules(Class<?> rules) {
        for (Field field : rules.getFields()) {
            if (!LIMIT.contains(field.getType())) continue;
            Rule annotation = field.getAnnotation(Rule.class);
            if (null == annotation) continue;
            String[] categories = annotation.categories();
            Class<? extends IValidator<?>>[] validators = annotation.validators();
            String[] suggestions = annotation.suggestions();
            CurtainRule<?> rule;
            if (annotation.serializedName()
                .contentEquals("")) {
                rule = CurtainRule.newRule(categories, validators, suggestions, field);
            } else {
                rule = CurtainRule.newRule(categories, validators, suggestions, field, annotation.serializedName());
            }
            String name = rule.getNormalName();
            for (String category : categories) {
                if (!CATEGORIES_RULES.containsKey(category)) CATEGORIES_RULES.put(category, new ArrayList<String>());
                CATEGORIES_RULES.get(category)
                    .add(name);
            }
            RULES.put(name, rule);
        }
    }

    /**
     * The rule file lives in the world folder. The world is not loaded yet when the manager is
     * created, so the folder name of the server is used instead of the save handler.
     */
    public File getFile() {
        String folder = this.server == null ? "world" : this.server.getFolderName();
        return new File(folder, id + ".json");
    }

    public void saveToFile() {
        JsonObject object = new JsonObject();
        for (String name : defaultRuleMap.keySet()) {
            CurtainRule<?> rule = defaultRuleMap.get(name);
            Object value = rule.getValue();
            if (value instanceof String) object.addProperty(name, (String) value);
            else if (value instanceof Boolean) object.addProperty(name, (Boolean) value);
            else if (value instanceof Byte) object.addProperty(name, (Byte) value);
            else if (value instanceof Short) object.addProperty(name, (Short) value);
            else if (value instanceof Integer) object.addProperty(name, (Integer) value);
            else if (value instanceof Long) object.addProperty(name, (Long) value);
            else if (value instanceof Float) object.addProperty(name, (Float) value);
            else if (value instanceof Double) object.addProperty(name, (Double) value);
            else throw RuleException.type();
        }
        File file = this.getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(object, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        File file = this.getFile();
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject object = GSON.fromJson(reader, JsonObject.class);
            if (object == null) return;
            for (String name : RULES.keySet()) {
                if (!object.has(name)) continue;
                JsonElement element = object.get(name);
                CurtainRule<?> rule = RULES.get(name);
                if (null == rule) throw RuleException.nu11();
                String raw = element.getAsString();
                this.setValue(rule, raw);
                defaultRuleMap.put(name, rule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setValue(CurtainRule<?> rule, String raw) {
        if (rule.validate(null, String.valueOf(raw))) rule.setValue(rule.parseValue(raw));
    }

    public String getId() {
        return this.id;
    }

    public void setDefault(String name) {
        CurtainRule<?> rule = RULES.get(name);
        if (rule == null) throw RuleException.nu11();
        this.defaultRuleMap.put(name, rule);
    }

    /** All rules, sorted by their serialized name. */
    public List<CurtainRule<?>> getSortedRules() {
        List<CurtainRule<?>> list = new ArrayList<CurtainRule<?>>(RULES.values());
        Collections.sort(
            list,
            (a, b) -> a.getNormalName()
                .compareToIgnoreCase(b.getNormalName()));
        return list;
    }

    public static List<String> getRuleNames() {
        List<String> list = new ArrayList<String>(RULES.keySet());
        Collections.sort(list);
        return list;
    }

    public static List<String> getCategories() {
        List<String> list = new ArrayList<String>(CATEGORIES_RULES.keySet());
        Collections.sort(list);
        return list;
    }

    public static List<String> getRuleNamesOfCategory(String category) {
        List<String> list = CATEGORIES_RULES.get(category);
        if (list == null) return Collections.emptyList();
        List<String> rt = new ArrayList<String>(list);
        Collections.sort(rt);
        return rt;
    }

    public static List<String> getAllCategoriesOrdered() {
        return Arrays.asList(
            Categories.FEATURE,
            Categories.BOT,
            Categories.CREATIVE,
            Categories.COMMAND,
            Categories.SURVIVAL,
            Categories.BUGFIX,
            Categories.CLIENT,
            Categories.TNT);
    }
}
