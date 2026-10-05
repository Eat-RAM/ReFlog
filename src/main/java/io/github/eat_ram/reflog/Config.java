package io.github.eat_ram.reflog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Config {
    public static final Logger LOGGER = LoggerFactory.getLogger("reflog");
    public static final Pattern VAR_PATTERN =
    Pattern.compile("\\$(?:\\{([0-9A-Za-z_]+)}|([0-9A-Za-z_]+))");

    public static void init() {}

    public static String
    expandVars(String str, HashMap<String, String> map, Properties ppts) {
        return VAR_PATTERN.matcher(str).replaceAll(mr -> map.computeIfAbsent(
            mr.group(mr.group(1) != null ? 1 : 2),
            k -> ppts.getProperty("alias." + k, "")
        ));
    }

    static {
        Properties ppts = new Properties();
        try {
            InputStream stream = new FileInputStream(
                FabricLoader.getInstance().getConfigDir()
                .resolve("reflog.properties").toFile()
            );
            ppts.load(stream);
        } catch (FileNotFoundException e) {
            LOGGER.info("No reflog.properties found.");
        } catch (IOException e) {
            LOGGER.error("Error reading reflog.properties", e);
        }
        HashMap<String, String> aliases = new HashMap<>();
        for (String key : ppts.stringPropertyNames()) {
            if (key.startsWith("alias.")) {
                aliases.put(key.substring(6), ppts.getProperty(key));
            } else if (key.startsWith("modify_existing.override.")) {
                String className = key.substring(25);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (Block.class.isAssignableFrom(clazz)) {
                        String value =
                        expandVars(ppts.getProperty(key), aliases, ppts);
                        String[] split = value.split(";");
                        ArrayList<String> list = new ArrayList<>(split.length);
                        for (int i = 1; i < split.length; ++i) {
                            list.add(FluidStateTranscript.escape(split[i]));
                        }
                        ModifyExisting.OVERRIDES
                        .put(clazz.asSubclass(Block.class), StringProperty.of(
                            split[0], ImmutableList.copyOf(list)
                        ));
                    } else {
                        LOGGER.error(
                            "Not a subclass of {}: {}", Block.class.getName(),
                            className
                        );
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.error("No such class: {}", className);
                }
            } else if (key.startsWith("modify_existing.override_block.")) {
                String value =
                expandVars(ppts.getProperty(key), aliases, ppts);
                String[] split = value.split(";");
                ArrayList<String> list = new ArrayList<>(split.length);
                for (int i = 1; i < split.length; ++i) {
                    list.add(FluidStateTranscript.escape(split[i]));
                }
                Identifier id = Identifier.tryParse(key.substring(31));
                if (id != null) {
                    ModifyExisting.OVERRIDES_PER_BLOCK
                    .put(id, StringProperty.of(
                        split[0], ImmutableList.copyOf(list)
                    ));
                }
            } else if (key.startsWith("modify_existing.retain_fluids_in.")) {
                String className = key.substring(33);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (Block.class.isAssignableFrom(clazz)) {
                        if (Boolean.parseBoolean(ppts.getProperty(key))) {
                            ModifyExisting.RETAIN_FLUIDS_IN
                            .add(clazz.asSubclass(Block.class));
                        }
                    } else {
                        LOGGER.error(
                            "Not a subclass of {}: {}", Block.class.getName(),
                            className
                        );
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.error("No such class: {}", className);
                }
            } else if (key.startsWith("modify_existing.disallow_drain.")) {
                String className = key.substring(31);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (Block.class.isAssignableFrom(clazz)) {
                        if (Boolean.parseBoolean(ppts.getProperty(key))) {
                            ModifyExisting.DISALLOW_DRAIN
                            .add(clazz.asSubclass(Block.class));
                        }
                    } else {
                        LOGGER.error(
                            "Not a subclass of {}: {}", Block.class.getName(),
                            className
                        );
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.error("No such class: {}", className);
                }
            } else if (key.startsWith("modify_existing.break_on_drain.")) {
                String className = key.substring(31);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (Block.class.isAssignableFrom(clazz)) {
                        if (Boolean.parseBoolean(ppts.getProperty(key))) {
                            ModifyExisting.BREAK_ON_DRAIN
                            .add(clazz.asSubclass(Block.class));
                        }
                    } else {
                        LOGGER.error(
                            "Not a subclass of {}: {}", Block.class.getName(),
                            className
                        );
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.error("No such class: {}", className);
                }
            } else if ("modify_existing.trusts_fluids".equals(key)) {
                for (String i : ppts.getProperty(
                    "modify_existing.trusts_fluids"
                ).split(";")) {
                    ModifyExisting.TRUSTS_FLUIDS.add(i);
                }
            }
        }
    }
}
