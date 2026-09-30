package io.github.eat_ram.reflog;

import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Config.init();
    }
}
