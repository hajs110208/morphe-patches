package app.template.patches.minecraft.backgroundplay

import app.morphe.patcher.Fingerprint

private const val MAIN = "Lcom/mojang/minecraftpe/MainActivity;"
private const val GAME_ACTIVITY = "Lcom/google/androidgamesdk/GameActivity;"

internal object MainActivityOnCreateFingerprint : Fingerprint(
    definingClass = MAIN, name = "onCreate", returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

internal object MainActivityOnWindowFocusChangedFingerprint : Fingerprint(
    definingClass = MAIN, name = "onWindowFocusChanged", returnType = "V",
    parameters = listOf("Z"),
)

internal object MainActivityOnPauseFingerprint : Fingerprint(
    definingClass = MAIN, name = "onPause", returnType = "V",
)

internal object MainActivityOnStopFingerprint : Fingerprint(
    definingClass = MAIN, name = "onStop", returnType = "V",
)

internal object GameActivityOnPauseFingerprint : Fingerprint(
    definingClass = GAME_ACTIVITY, name = "onPause", returnType = "V", parameters = listOf(),
)

internal object GameActivityOnStopFingerprint : Fingerprint(
    definingClass = GAME_ACTIVITY, name = "onStop", returnType = "V", parameters = listOf(),
)
