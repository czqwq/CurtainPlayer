package com.Lilith.Curtain.api.rules;

import static com.Lilith.Curtain.utils.TranslationKeys.RULE_DESC;
import static com.Lilith.Curtain.utils.TranslationKeys.RULE_NAME;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.utils.TranslationHelper;
import com.google.common.base.CaseFormat;

public class CurtainRule<T> {

    private final String[] categories;
    private final List<IValidator<T>> validators;
    private final String[] suggestions;
    private final Field field;
    private final T defaultValue;

    private final String nameTranslationKey;
    private final String descTranslationKey;

    private CurtainRule(String[] categories, List<IValidator<T>> validators, String[] suggestions, Field field) {
        this(
            categories,
            validators,
            suggestions,
            field,
            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
    }

    @SuppressWarnings("unchecked")
    private CurtainRule(String[] categories, List<IValidator<T>> validators, String[] suggestions, Field field,
        String serializedName) {
        this.categories = categories;
        this.validators = validators;
        this.suggestions = field.getType() == boolean.class || field.getType() == Boolean.class
            ? new String[] { "true", "false" }
            : suggestions;
        this.field = field;
        nameTranslationKey = String.format(RULE_NAME, Curtain.MODID, serializedName);
        descTranslationKey = String.format(RULE_DESC, Curtain.MODID, serializedName);
        try {
            this.defaultValue = (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuleException(e.getMessage());
        }
    }

    public void setValue(Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuleException(e.getMessage());
        }
    }

    public boolean validate(ICommandSender source, String newValue) {
        for (IValidator<T> validator : validators) {
            if (!validator.validate(source, this, newValue)) return false;
        }
        return true;
    }

    public static <T> CurtainRule<T> newRule(String[] categories, Class<? extends IValidator<?>>[] validators,
        String[] suggestions, Field field, String serializedName) {
        List<IValidator<T>> validators1 = buildValidators(validators);
        return new CurtainRule<T>(categories, validators1, suggestions, field, serializedName);
    }

    public static <T> CurtainRule<T> newRule(String[] categories, Class<? extends IValidator<?>>[] validators,
        String[] suggestions, Field field) {
        List<IValidator<T>> validators1 = buildValidators(validators);
        return new CurtainRule<T>(categories, validators1, suggestions, field);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<IValidator<T>> buildValidators(Class<? extends IValidator<?>>[] validators) {
        List<IValidator<T>> validators1 = new ArrayList<IValidator<T>>();
        for (Class<? extends IValidator<?>> validator : validators) {
            try {
                validators1.add(
                    (IValidator<T>) validator.getDeclaredConstructor()
                        .newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return validators1;
    }

    @Override
    public String toString() {
        Object v = getValue();
        return v == null ? "null" : v.toString();
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        try {
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reset() {
        this.setValue(defaultValue);
    }

    /**
     * Parses the given raw string into the type of this rule.
     *
     * @throws IllegalArgumentException if the string can not be parsed
     */
    public Object parseValue(String str) {
        Class<?> type = field.getType();
        if (type == String.class) return str;
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(str);
        if (type == Byte.class || type == byte.class) return Byte.parseByte(str);
        if (type == Short.class || type == short.class) return Short.parseShort(str);
        if (type == Integer.class || type == int.class) return Integer.parseInt(str);
        if (type == Long.class || type == long.class) return Long.parseLong(str);
        if (type == Float.class || type == float.class) return Float.parseFloat(str);
        if (type == Double.class || type == double.class) return Double.parseDouble(str);
        throw RuleException.legal();
    }

    public String[] getCategories() {
        return categories;
    }

    public IChatComponent getNameComponent() {
        return TranslationHelper.translate(this.getNameTranslationKey());
    }

    public IChatComponent getDescComponent() {
        return TranslationHelper.translate(this.getDescTranslationKey());
    }

    public String getNameTranslationKey() {
        return nameTranslationKey;
    }

    public String getDescTranslationKey() {
        return descTranslationKey;
    }

    public Class<?> getType() {
        return this.field.getType();
    }

    public String getName() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }

    public boolean isDefault(String s) {
        return String.valueOf(this.defaultValue)
            .equals(s);
    }

    public String getNormalName() {
        return field.getName();
    }

    public String[] getSuggestions() {
        return suggestions;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * The values that make sense for this rule, used for tab completion.
     */
    public Collection<String> getExamples() {
        ArrayList<String> rt = new ArrayList<String>();
        if (this.getType() == String.class) {
            for (String s : this.suggestions) rt.add("\"" + s + "\"");
        } else {
            for (String s : this.suggestions) rt.add(s);
        }
        return rt;
    }

    /**
     * Plain (unquoted) suggestions, used by the 1.7.10 command tab completion.
     */
    public List<String> getPlainSuggestions() {
        List<String> rt = new ArrayList<String>();
        for (String s : this.suggestions) rt.add(s);
        return rt;
    }
}
