package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.Waterloggable;
import net.minecraft.state.State;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

@Mixin(net.minecraft.state.StateManager.Builder.class)
public abstract class BuilderMixin<O, S extends State<O, S>> {
    @Shadow
    @Final
    private O owner;

    @ModifyVariable(
        method = "add", at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private Property<?>[] removeWaterlogged(Property<?>... properties) {
        O owner = this.owner;
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(owner);
        if (owner instanceof Block && owner instanceof Waterloggable &&
            fluidlogged != null &&
            fluidlogged.getName().equals(Properties.WATERLOGGED.getName())) {
            for (Property<?> property : properties) {
                if (property == Properties.WATERLOGGED) {
                    Property<?>[] newProperties =
                        new Property[properties.length - 1];
                    int i = 0;
                    for (Property<?> property1 : properties) {
                        if (property1 != Properties.WATERLOGGED) {
                            newProperties[i] = property1;
                            ++i;
                        }
                    }
                    return newProperties;
                }
            }
        }
        return properties;
    }
}
