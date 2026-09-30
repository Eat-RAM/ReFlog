package io.github.eat_ram.reflog.state;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Contract;

public class StringProperty extends Property<String> {
    private static final HashMap<Pair<String, List<String>>, StringProperty>
    PROPERTY_MAP = new HashMap<>();
    private final List<String> valuesList;
    private final Object2IntOpenHashMap<String> valuesToOrdinal;

    public static StringProperty
    of(String name, ImmutableList<String> values) {
        Pair<String, List<String>> pair = Pair.of(name, values);
        StringProperty property = PROPERTY_MAP.get(pair);
        if (property != null) {
            return property;
        }
        property = new StringProperty(name, values);
        PROPERTY_MAP.put(pair, property);
        return property;
    }

//    protected StringProperty(String name, Iterable<String> values) {
//        this(name, ImmutableList.copyOf(values));
//    }

    protected StringProperty(String name, ImmutableList<String> values) {
        super(name, String.class);
        this.valuesList = values;
        this.valuesToOrdinal = new Object2IntOpenHashMap<>();
        this.valuesToOrdinal.defaultReturnValue(-1);
        int i = 0;
        for (String value : values) {
            this.valuesToOrdinal.put(value, i);
            ++i;
        }
    }

    @Override
    public List<String> getValues() {
        return this.valuesList;
    }

    @Override
    public String name(String value) {
        return value;
    }

    @Override
    public Optional<String> parse(String name) {
        return this.valuesList.contains(name) ? Optional.of(name) :
               Optional.empty();
    }

    @Override
    public int ordinal(String value) {
        return this.valuesToOrdinal.getInt(value);
    }

    @Override
    @Contract("null -> false")
    public boolean equals(Object o) {
        return this == o || (
            o instanceof StringProperty && super.equals(o) &&
            this.valuesList.equals(((StringProperty)o).valuesList)
        );
    }
}
