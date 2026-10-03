package io.github.eat_ram.reflog.block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;

import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ModifyExisting {
    public static final HashMap<
        @NotNull Class<? extends Block>, @NotNull StringProperty
    > OVERRIDES = new HashMap<>();
    public static final HashMap<@NotNull Identifier, @NotNull StringProperty>
    OVERRIDES_PER_BLOCK = new HashMap<>(); // post-1.21.2 only
    public static final HashSet<@NotNull Class<? extends Block>>
    RETAIN_FLUIDS_IN = new HashSet<>();
    public static final HashSet<@NotNull Class<? extends Block>>
    DISALLOW_DRAIN = new HashSet<>();
    public static final HashSet<@NotNull Class<? extends Block>>
    BREAK_ON_DRAIN = new HashSet<>();
    public static final HashSet<@NotNull String> TRUSTS_FLUIDS =
    new HashSet<>();

    public static final
    IdentityHashMap<@NotNull Block, @NotNull StringProperty> CACHED_PPTS =
    new IdentityHashMap<>();

    @Contract("-> fail")
    private ModifyExisting() {
        throw new UnsupportedOperationException();
    }
}
