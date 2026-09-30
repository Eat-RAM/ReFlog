package io.github.eat_ram.reflog.block;

import java.util.HashMap;
import java.util.HashSet;

import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ModifyExisting {
    public static final HashMap<
        @NotNull Class<? extends Block>, @NotNull StringProperty
    > OVERRIDES = new HashMap<>();
    public static final HashSet<@NotNull Class<? extends Block>>
    RETAIN_FLUIDS_IN = new HashSet<>();

    @Contract("-> fail")
    private ModifyExisting() {
        throw new UnsupportedOperationException();
    }
}
