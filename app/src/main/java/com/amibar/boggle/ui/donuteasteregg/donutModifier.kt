package com.amibar.boggle.ui.donuteasteregg

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.opengl.Matrix
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.NoInspectorInfo

@Composable
internal fun Modifier.donutWrapped() = composed(NoInspectorInfo, factory = {
    val assets = LocalContext.current.assets

    val shader = remember {
        assets.open("donutShader.agsl").bufferedReader().use { it.readText() }.let { RuntimeShader(it) }
    }

    var rotX by remember { mutableFloatStateOf(0f) }
    var rotY by remember { mutableFloatStateOf(0f) }
    var rotZ by remember { mutableFloatStateOf(0f) }
    var distanceOffset by remember { mutableFloatStateOf(0f) }

    this
        .pointerInput(Unit) {
            detectTransformGestures(false) { _, pan, zoom, rotation ->
                // Orbit rotation: pan pixels to degrees
                rotY += pan.x * 0.5f
                rotX -= pan.y * 0.5f
                rotZ += rotation

                distanceOffset += (zoom - 1f) * 15f
            }
        }
        .graphicsLayer {
            val matrix = FloatArray(16).apply {
                Matrix.setIdentityM(this, 0)
                
                // 1. Rotate the coordinate system for the orbit
                Matrix.rotateM(this, 0, rotX, 1f, 0f, 0f)
                Matrix.rotateM(this, 0, rotY, 0f, 1f, 0f)
                Matrix.rotateM(this, 0, rotZ, 0f, 0f, 1f)
                
                // 2. Translate the camera along its new local Z axis
                Matrix.translateM(this, 0, 0f, 0f, distanceOffset)
            }

            try {
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("translationMatrix", matrix)
            } catch (_: IllegalArgumentException) {
                // ignored
            }

            renderEffect = RenderEffect.createRuntimeShaderEffect(
                shader,
                "composable"
            ).asComposeRenderEffect()
            clip = true
        }
})
