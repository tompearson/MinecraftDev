package test;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MixedInOuter.MixedInInner.class)
public class TestMixin {

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void injectCtor(String string, CallbackInfo ci<caret>, String local1, float local2, int local3) {
    }
}
