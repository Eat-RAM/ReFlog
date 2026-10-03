package io.github.eat_ram.reflog.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.Waterloggable;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

@Mixin(net.minecraft.state.State.class)
public abstract class StateMixin<O, S> {
    @Shadow
    @Final
    protected O owner;

    @Shadow
    protected abstract int indexOfProperty(Property<?> property);

    @Shadow
    @Final
    private Comparable<?>[] values;

    @Shadow
    public abstract <T extends Comparable<T>, V extends T> S with(Property<T> property, V value);

    @Inject(method = "getNullable", at = @At("HEAD"), cancellable = true)
    private <T extends Comparable<T>> void handleWaterlogged(
        final Property<T> property, CallbackInfoReturnable<T> cir
    ) {
        O owner = this.owner;
        if (owner instanceof Block && owner instanceof Waterloggable &&
            property == Properties.WATERLOGGED) {
            StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(owner);
            if (fluidlogged != null &&
                fluidlogged.getName().equals(property.getName())) {
                //System.err.println(Arrays.toString(this.values));
                if (this.values == null) {
                    cir.setReturnValue(property.getType().cast(Boolean.FALSE));
                    return;
                }
                int index = this.indexOfProperty(fluidlogged);
                cir.setReturnValue(index != -1 ? property.getType().cast(
                    "true".equals(this.values[index]) ? Boolean.TRUE :
                    Boolean.FALSE
                ) : null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(
        method = "with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
        at = @At("HEAD"), cancellable = true
    )
    private  <T extends Comparable<T>, V extends T> void handleWaterlogged(
        final Property<T> property, final V value,
        CallbackInfoReturnable<S> cir
    ) {
        O owner = this.owner;
        if (owner instanceof Block && owner instanceof Waterloggable &&
            property == Properties.WATERLOGGED) {
            StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(owner);
            if (fluidlogged != null &&
                fluidlogged.getName().equals(property.getName())) {
                cir.setReturnValue(Boolean.FALSE.equals(value) ? this.with(
                    fluidlogged, fluidlogged.getValues().get(0)
                ) : (S)this);
            }
        }
    }
}
