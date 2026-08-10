package local.integratedscripting.graalfix;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = IntegratedScriptingGraalFix.MOD_ID, dist = Dist.CLIENT)
public final class IntegratedScriptingGraalFix {
    public static final String MOD_ID = "integratedscripting_graalfix";
    private static final Logger LOGGER = LoggerFactory.getLogger("IntegratedScriptingGraalFix");

    public IntegratedScriptingGraalFix() {
        LOGGER.info(
                "Inactive for Integrated Scripting 1.0.24: its upstream fallback-classloader rewrite removed the "
                        + "relocated Truffle target, so no compatibility patch is applied; this transition mod can be removed");
    }
}
