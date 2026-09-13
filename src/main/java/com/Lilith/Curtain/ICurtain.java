package com.Lilith.Curtain;

import java.io.InputStream;

import com.Lilith.Curtain.api.rules.RuleManager;
import com.Lilith.Curtain.utils.TranslationHelper;

public interface ICurtain {

    /**
     * 解析翻译文件
     */
    default void parseTrans(String name, InputStream stream) {
        TranslationHelper.addTransMap(name, TranslationHelper.getTranslationFromResourcePath(stream));
    }

    /**
     * 添加规则
     */
    default void addRules(Class<?> rulesClass) {
        RuleManager.addRules(rulesClass);
    }
}
