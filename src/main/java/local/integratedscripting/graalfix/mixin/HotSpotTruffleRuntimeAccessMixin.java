package local.integratedscripting.graalfix.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Mixin(targets = "org.cyclops.integratedscripting.vendors.com.oracle.truffle.runtime.hotspot.HotSpotTruffleRuntimeAccess", remap = false)
abstract class HotSpotTruffleRuntimeAccessMixin {
    private static final Logger GRAALFIX_LOGGER = LoggerFactory.getLogger("IntegratedScriptingGraalFix");
    private static final String FALLBACK_CLASS =
            "org.cyclops.integratedscripting.vendors.com.oracle.truffle.api.impl.DefaultTruffleRuntime";

    @Inject(method = "createRuntime", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void integratedscriptingGraalFix$useEmbeddedInterpreter(CallbackInfoReturnable<Object> cir) {
        try {
            ClassLoader loader = HotSpotTruffleRuntimeAccessMixin.class.getClassLoader();
            Class<?> fallbackClass = Class.forName(FALLBACK_CLASS, true, loader);
            Constructor<?> constructor = fallbackClass.getConstructor(String.class);
            Object fallback = constructor.newInstance(
                    "Integrated Scripting's optimized Truffle runtime is incompatible with relocated classes on GraalVM; " +
                            "the local compatibility mod selected its embedded interpreter runtime.");
            GRAALFIX_LOGGER.info("Active: using Integrated Scripting's embedded interpreter runtime; Minecraft JVMCI is unchanged");
            cir.setReturnValue(fallback);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Integrated Scripting 1.0.22 changed: unable to create its expected embedded interpreter runtime", exception);
        }
    }
}
