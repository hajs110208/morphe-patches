package app.template.patches.minecraft.backgroundplay

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.w3c.dom.Element

private const val EXT = "Lapp/template/extension/minecraft/BackgroundPlay;"
private const val SERVICE = "app.template.extension.minecraft.BackgroundPlayService"

private val backgroundPlayManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.documentElement
            val app = manifest.getElementsByTagName("application").item(0) as Element

            listOf(
                "android.permission.FOREGROUND_SERVICE",
                "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
                "android.permission.POST_NOTIFICATIONS",
                "android.permission.WAKE_LOCK",
            ).forEach { name ->
                val exists = (0 until manifest.getElementsByTagName("uses-permission").length).any {
                    (manifest.getElementsByTagName("uses-permission").item(it) as Element)
                        .getAttribute("android:name") == name
                }
                if (!exists) {
                    manifest.insertBefore(
                        doc.createElement("uses-permission").apply { setAttribute("android:name", name) },
                        app,
                    )
                }
            }

            val service = doc.createElement("service").apply {
                setAttribute("android:name", SERVICE)
                setAttribute("android:exported", "false")
                setAttribute("android:foregroundServiceType", "specialUse")
                appendChild(doc.createElement("property").apply {
                    setAttribute("android:name", "android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE")
                    setAttribute("android:value", "game_background_play")
                })
            }
            app.appendChild(service)
        }
    }
}

@Suppress("unused")
val backgroundPlayPatch = bytecodePatch(
    name = "Foreground service",
    description = "Keeps game running in the background.",
    default = false,
) {
    compatibleWith("com.mojang.minecraftpe")
    dependsOn(backgroundPlayManifestPatch)
    extendWith("extensions/extension.mpe")

    execute {
        MainActivityOnWindowFocusChangedFingerprint.method.addInstructions(0, "const/4 p1, 0x1")
        MainActivityOnPauseFingerprint.method.removeCalls("nativeSuspend")
        MainActivityOnStopFingerprint.method.removeCalls("nativeStopThis")
        GameActivityOnPauseFingerprint.method.replaceNativeCall("onPauseNative")
        GameActivityOnStopFingerprint.method.replaceNativeCall("onStopNative")
        MainActivityOnCreateFingerprint.method.apply {
            val last = implementation!!.instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            if (last < 0) throw PatchException("onCreate return-void not found")
            addInstructions(last, "invoke-static {p0}, $EXT->onActivityCreate(Landroid/app/Activity;)V")
        }
    }
}

private fun com.android.tools.smali.dexlib2.iface.Method.indicesOfCall(prefix: String): List<Int> =
    implementation!!.instructions.withIndex().filter { (_, ins) ->
        ins is ReferenceInstruction && (ins.reference as? MethodReference)?.name?.startsWith(prefix) == true
    }.map { it.index }

private fun app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.removeCalls(prefix: String) {
    val idx = indicesOfCall(prefix)
    if (idx.isEmpty()) throw PatchException("call to $prefix* not found in $name")
    idx.sortedDescending().forEach { removeInstruction(it) }
}

private fun app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.replaceNativeCall(target: String) {
    val idx = indicesOfCall(target).firstOrNull()
        ?: throw PatchException("call to $target not found in $definingClass->$name")
    val ins = getInstruction<FiveRegisterInstruction>(idx)
    val ref = (ins as ReferenceInstruction).reference as MethodReference
    if (ref.parameterTypes.joinToString("") != "J") {
        throw PatchException("unexpected signature of $target: ${ref.parameterTypes}")
    }
    val regs = listOf(ins.registerC, ins.registerD, ins.registerE, ins.registerF, ins.registerG)
        .take(ins.registerCount).joinToString(", ") { "v$it" }
    replaceInstruction(idx, "invoke-static {$regs}, $EXT->skipNative(Ljava/lang/Object;J)V")
}
