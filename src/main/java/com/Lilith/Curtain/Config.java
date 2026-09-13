package com.Lilith.Curtain;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static Configuration configuration;

    public static void synchronizeConfiguration(File configFile) {
        configuration = new Configuration(configFile);
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
