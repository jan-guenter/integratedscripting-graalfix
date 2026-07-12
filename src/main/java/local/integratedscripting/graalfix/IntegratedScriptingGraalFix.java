package local.integratedscripting.graalfix;

import net.neoforged.fml.common.Mod;

@Mod(IntegratedScriptingGraalFix.MOD_ID)
public final class IntegratedScriptingGraalFix {
    public static final String MOD_ID = "integratedscripting_graalfix";

    public IntegratedScriptingGraalFix() {
        // The required mixin logs when it actually intercepts Truffle initialization.
    }
}
