package app.morphe.patches.minecraft.branding

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.File

private const val ICON_RESOURCE_PATH = "res/drawable-xxhdpi-v4/icon.png"
private const val DEFAULT_ICON_RESOURCE = "minecraft/icon/default_icon.png"

private val PNG_SIGNATURE = byteArrayOf(
    0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
)

private fun ByteArray.isPng() =
    size >= PNG_SIGNATURE.size && PNG_SIGNATURE.indices.all { this[it] == PNG_SIGNATURE[it] }

@Suppress("unused")
val changeAppIconPatch = resourcePatch(
    name = "Change app icon",
    description = "Replaces the app icon shown in the launcher into the patch logo. You can set a custom icon if you want other icons.",
    default = false,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    val iconPathOption = stringOption(
        key = "iconPath",
        default = null,
        title = "Custom icon",
        description = "Icon file (png) " +
            "Leave empty to use the default icon.",
        required = false,
    ) { path ->
        if (path.isNullOrBlank()) {
            true
        } else {
            val file = File(path.trim())
            file.isFile && file.inputStream().use { input ->
                input.readNBytes(PNG_SIGNATURE.size).isPng()
            }
        }
    }

    execute {
        val customPath = iconPathOption.value?.trim().orEmpty()

        val iconBytes = if (customPath.isNotEmpty()) {
            val file = File(customPath)
            if (!file.isFile) throw PatchException("Icon file not found: $customPath")
            file.readBytes().also {
                if (!it.isPng()) throw PatchException("Icon file is not a valid PNG: $customPath")
            }
        } else {
            object {}.javaClass.classLoader.getResourceAsStream(DEFAULT_ICON_RESOURCE)
                ?.use { it.readBytes() }
                ?: throw PatchException("Default icon resource not found: $DEFAULT_ICON_RESOURCE")
        }
        get(ICON_RESOURCE_PATH).apply {
            parentFile?.mkdirs()
            writeBytes(iconBytes)
        }
    }
}
