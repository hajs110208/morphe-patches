package app.morphe.patches.minecraft.mbl2

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val mtbinloader2Patch = bytecodePatch(
    name = "Add mtbinloader2 lib",
    description = "Add libmtbinloader2.so to the app's native libraries.",
    default = true,
) {
    execute {
        nativeLibraries.forEach { abi ->
    val resourcePath = "minecraft/lib/$abi/libmtbinloader2.so"
    val input = this::class.java.classLoader?.getResourceAsStream(resourcePath)

    if (input != null) {
        input.use { source ->
            val destination = get(resourcePath, true)
            destination.outputStream().use<java.io.OutputStream, Unit> { output ->
                source.copyTo(output)
            }
        }
    }
}
        MinecraftPlatformOnCreateFingerprint.method.addInstructions(
            0,
            """
            const-string v2, "mtbinloader2"
            invoke-static {v2}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """,
        )
    }
}
