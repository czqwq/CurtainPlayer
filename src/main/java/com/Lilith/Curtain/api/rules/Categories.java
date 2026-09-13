package com.Lilith.Curtain.api.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Categories {

    public static final String FEATURE = "feature";
    public static final String BOT = "bot";
    public static final String CREATIVE = "creative";
    public static final String COMMAND = "command";
    public static final String SURVIVAL = "survival";
    public static final String BUGFIX = "bugfix";
    public static final String CLIENT = "client";
    public static final String TNT = "tnt";

    public static List<String> getCategories() {
        ArrayList<String> rt = new ArrayList<String>();
        for (Field field : Categories.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) continue;
            try {
                rt.add((String) field.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return rt;
    }
}
