package io.github.eat_ram.reflog.state;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

public abstract class FluidStateTranscript {
    public static final Pattern PIPE = Pattern.compile("\\|");
    public static final Pattern EQUAL = Pattern.compile("=");
    private static final HashMap<String, FluidState> UNESCAPED_TO_STATE =
    new HashMap<>();
    private static final HashMap<String, FluidState> ESCAPED_TO_STATE =
    new HashMap<>();

    public static <T extends Comparable<T>> String
    getPropertyValueName(State<?, ?> state, Property<T> property) {
        return property.name(state.get(property));
    }

    public static <S extends State<?, S>, T extends Comparable<T>> S
    withIfSucceed(S state, Property<T> property, String value) {
        Optional<T> t = property.parse(value);
        return t.isPresent() ? state.withIfExists(property, t.get()) : state;
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '_':
                    sb.append("__");
                    break;
                case '-':
                    sb.append("_h");
                    break;
                case ':':
                    sb.append("_c");
                    break;
                case '.':
                    sb.append("_d");
                    break;
                case '/':
                    sb.append("_s");
                    break;
                case '|':
                    sb.append("_v");
                    break;
                case '=':
                    sb.append("_e");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (char c : s.toCharArray()) {
            if (escaped) {
                switch (c) {
                    case '_':
                        sb.append('_');
                        break;
                    case 'h':
                        sb.append('-');
                        break;
                    case 'c':
                        sb.append(':');
                        break;
                    case 'd':
                        sb.append('.');
                        break;
                    case 's':
                        sb.append('/');
                        break;
                    case 'v':
                        sb.append('|');
                        break;
                    case 'e':
                        sb.append('=');
                        break;
                }
                escaped = false;
            } else if (c == '_') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String transcript(FluidState state) {
        StringBuilder sb = new StringBuilder();
        Fluid fluid = state.getFluid();
        sb.append(Registries.FLUID.getId(fluid));
        ArrayList<Property<?>> ppts = new ArrayList<>(state.getProperties());
        ppts.sort(Comparator.comparing(Property::getName));
        for (Property<?> ppt : ppts) {
            sb.append('|');
            sb.append(ppt.getName());
            sb.append('=');
            sb.append(getPropertyValueName(state, ppt));
        }
        return sb.toString();
    }

    public static String transcriptWaterlogged(FluidState state) {
        if (state == Fluids.WATER.getStill(false)) {
            return "true";
        }
        if (state == Fluids.EMPTY.getDefaultState()) {
            return "false";
        }
        return transcript(state);
    }

    public static FluidState restore(String s, boolean addCache) {
        FluidState cached = UNESCAPED_TO_STATE.get(s);
        if (cached != null) {
            return cached;
        }
        String[] seps = PIPE.split(s);
        Identifier id = Identifier.tryParse(seps[0]);
        FluidState state = Registries.FLUID.get(id).getDefaultState();
        HashMap<String, Property<?>> properties = new HashMap<>();
        for (Property<?> i : state.getProperties()) {
            properties.put(i.getName(), i);
        }
        for (int i = 1; i < seps.length; ++i) {
            String[] seps2 = EQUAL.split(seps[i], 2);
            if (seps2.length == 2) {
                Property<?> ppt = properties.get(seps2[0]);
                if (ppt != null) {
                    state = withIfSucceed(state, ppt, seps2[1]);
                }
            }
        }
        if (addCache) {
            UNESCAPED_TO_STATE.put(s, state);
        }
        return state;
    }

    public static FluidState restoreWaterlogged(String s, boolean addCache) {
        if ("true".equals(s)) {
            return Fluids.WATER.getStill(false);
        }
        if ("false".equals(s)) {
            return Fluids.EMPTY.getDefaultState();
        }
        return restore(s, addCache);
    }

    @Contract("-> fail")
    private FluidStateTranscript() {
        throw new UnsupportedOperationException();
    }
}
