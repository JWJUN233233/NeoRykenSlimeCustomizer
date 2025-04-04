package org.lins.mmmjjkx.rykenslimefuncustomizer.api.script;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.objects.JSAttributes;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.JSObjectUtil;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.script.ScriptException;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.io.IOAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.abstracts.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.BlockMenuUtil;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class JavaScriptEval extends ScriptEval {
    private static final File PLUGINS_FOLDER =
            RykenSlimefunCustomizer.INSTANCE.getDataFolder().getParentFile();
    private final Set<String> failed_functions = new HashSet<>();

    private GraalJSScriptEngine jsEngine;

    public JavaScriptEval(@NotNull File js, ProjectAddon addon) {
        super(js, addon);
        reSetup();

        setup();

        contextInit();

        addon.getScriptEvals().add(this);
    }

    private void advancedSetup() {
        JSRealm realm = JavaScriptLanguage.getJSRealm(jsEngine.getPolyglotContext());
        TruffleLanguage.Env env = realm.getEnv();
        addThing("SlimefunItems", env.asHostSymbol(SlimefunItems.class));
        addThing("SlimefunItem", env.asHostSymbol(SlimefunItem.class));
        addThing("StorageCacheUtils", env.asHostSymbol(StorageCacheUtils.class));
        addThing("SlimefunUtils", env.asHostSymbol(SlimefunUtils.class));
        addThing("BlockMenu", env.asHostSymbol(BlockMenu.class));
        addThing("BlockMenuUtil", env.asHostSymbol(BlockMenuUtil.class));
        addThing("PlayerProfile", env.asHostSymbol(PlayerProfile.class));
        addThing("Slimefun", env.asHostSymbol(Slimefun.class));

        for (File file : Objects.requireNonNull(PLUGINS_FOLDER.listFiles())) {
            TruffleFile truffleFile = env.getPublicTruffleFile(file.toURI());
            if (!truffleFile.isDirectory() && truffleFile.getName().endsWith(".jar")) {
                env.addToHostClassPath(truffleFile);
            }
        }

        JSObject java = JSObjectUtil.createOrdinaryPrototypeObject(realm);
        JSObjectUtil.putToStringTag(java, JSRealm.JAVA_CLASS_NAME);

        JSObjectUtil.putDataProperty(realm.getGlobalObject(), "Java", java, JSAttributes.getDefaultNotEnumerable());
    }

    @Override
    public void close() {
        try {
            jsEngine.close();
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void addThing(String name, Object value) {
        jsEngine.put(name, value);
    }

    @Override
    public String key() {
        return "js";
    }

    protected final void contextInit() {
        super.contextInit();
        if (jsEngine != null) {
            try {
                jsEngine.eval(getFileContext());
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable @CanIgnoreReturnValue
    @Override
    public Object evalFunction(String funName, Object... args) {
        if (getFileContext() == null || getFileContext().isBlank()) {
            contextInit();
        }

        // a simple fix for the optimization
        if (failed_functions.contains(funName)) {
            return null;
        }

        try {
            Object result = jsEngine.invokeFunction(funName, args);
            ExceptionHandler.debugLog(
                    "运行了 " + getAddon().getAddonName() + "的脚本" + getFile().getName() + "中的函数 " + funName);
            return result;
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (!message.contains("Multi threaded access")) {
                ExceptionHandler.handleError(
                        "在运行附属" + getAddon().getAddonName() + "的脚本" + getFile().getName() + "时发生错误");
                e.printStackTrace();
            }
        } catch (ScriptException e) {
            ExceptionHandler.handleError(
                    "在运行" + getAddon().getAddonName() + "的脚本" + getFile().getName() + "时发生错误");
            e.printStackTrace();
        } catch (NoSuchMethodException ignored) {
            // won't log it, because listeners always send a lot of functions
            failed_functions.add(funName);
        } catch (Throwable e) {
            ExceptionHandler.handleError(
                    "在运行" + getAddon().getAddonName() + "的脚本" + getFile().getName() + "时发生意外错误");
            e.printStackTrace();
        }

        return null;
    }

    private void reSetup() {
        jsEngine = GraalJSScriptEngine.create(
                Engine.newBuilder("js").allowExperimentalOptions(true).build(),
                Context.newBuilder("js")
                        .hostClassLoader(RykenSlimefunCustomizer.class.getClassLoader())
                        .allowAllAccess(true)
                        .allowHostAccess(UNIVERSAL_HOST_ACCESS)
                        .allowNativeAccess(false)
                        .allowExperimentalOptions(true)
                        .allowPolyglotAccess(PolyglotAccess.ALL)
                        .allowCreateProcess(true)
                        .allowValueSharing(true)
                        .allowIO(IOAccess.ALL)
                        .allowHostClassLookup(s -> true)
                        .allowHostClassLoading(true));

        advancedSetup();
    }
}
