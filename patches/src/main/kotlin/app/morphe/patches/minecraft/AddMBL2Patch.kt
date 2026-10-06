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
        val nativeLibraries = listOf(
            "arm64-v8a",
            "armeabi-v7a",
            "x86_64"
        )

        nativeLibraries.forEach { abi ->
            val resourcePath = "minecraft/lib/$abi/libmtbinloader2.so"
            val input = classLoader.getResourceAsStream(resourcePath)

            if (input != null) {
                input.use { source ->
                    val destination = get(resourcePath, true)
                    destination.outputStream().use { output ->
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
