package com.amibar.boggle.ui.donuteasteregg

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.opengl.Matrix
import android.view.MotionEvent
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.NoInspectorInfo
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
internal fun Modifier.donutWrapped() = composed(NoInspectorInfo, factory = {
    val assets = LocalContext.current.assets
    val view = LocalView.current

    val shader = remember {
        assets.open("donutShader.agsl").bufferedReader().use { it.readText() }
            .let { RuntimeShader(it) }
    }

    var rotX by remember { mutableFloatStateOf(0f) }
    var rotY by remember { mutableFloatStateOf(0f) }
    var rotZ by remember { mutableFloatStateOf(0f) }
    var distanceOffset by remember { mutableFloatStateOf(0f) }

    var isForwarding by remember { mutableStateOf(false) }
    var viewOffset by remember { mutableStateOf(Offset.Zero) }

    // Track synthetic gesture lifecycle
    var isSyntheticDown by remember { mutableStateOf(false) }
    var syntheticDownTime by remember { mutableLongStateOf(0L) }

    val getMatrix = {
        FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.rotateM(this, 0, rotX, 1f, 0f, 0f)
            Matrix.rotateM(this, 0, rotY, 0f, 1f, 0f)
            Matrix.rotateM(this, 0, rotZ, 0f, 0f, 1f)
            Matrix.translateM(this, 0, 0f, 0f, distanceOffset)
        }
    }

    this
        .onGloballyPositioned { viewOffset = it.positionInRoot() }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (isForwarding) continue

                    val change = event.changes.firstOrNull() ?: continue
                    val projected = projectTouch(change.position, size.toSize(), getMatrix())

                    if (projected != null) {
                        event.motionEvent?.let { original ->
                            val action = when {
                                !isSyntheticDown -> {
                                    isSyntheticDown = true
                                    syntheticDownTime = original.eventTime
                                    MotionEvent.ACTION_DOWN
                                }
                                !change.pressed -> {
                                    isSyntheticDown = false
                                    MotionEvent.ACTION_UP
                                }
                                else -> MotionEvent.ACTION_MOVE
                            }

                            // Create a synthetic event with a slightly shifted timestamp
                            // and corrected coordinates relative to the View root.
                            val synthetic = MotionEvent.obtain(
                                syntheticDownTime,
                                original.eventTime + 1, // Offset time to bypass duplicate event filters
                                action,
                                projected.x + viewOffset.x,
                                projected.y + viewOffset.y,
                                original.metaState
                            )

                            view.post {
                                isForwarding = true
                                try {
                                    view.dispatchTouchEvent(synthetic)
                                } finally {
                                    isForwarding = false
                                    synthetic.recycle()
                                }
                            }
                        }
                        event.changes.forEach { it.consume() }
                    } else {
                        // End synthetic stream if finger moves off the donut
                        if (isSyntheticDown) {
                            event.motionEvent?.let { original ->
                                val synthetic = MotionEvent.obtain(
                                    syntheticDownTime,
                                    original.eventTime + 1,
                                    MotionEvent.ACTION_UP,
                                    0f, 0f, 0 // Exact coords matter less for release
                                )
                                view.post {
                                    isForwarding = true
                                    view.dispatchTouchEvent(synthetic)
                                    isForwarding = false
                                    synthetic.recycle()
                                }
                            }
                            isSyntheticDown = false
                        }

                        // Standard camera controls
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        val rotation = event.calculateRotation()

                        rotY += pan.x * 0.5f
                        rotX -= pan.y * 0.5f
                        rotZ += rotation
                        distanceOffset += (zoom - 1f) * 15f
                    }
                }
            }
        }
        .graphicsLayer {
            val matrix = getMatrix()
            try {
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("transformationMatrix", matrix)
            } catch (_: IllegalArgumentException) {}

            renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
            clip = true
        }
})

private fun projectTouch(offset: Offset, size: Size, matrix: FloatArray): Offset? {
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return null

    val uvX = (offset.x - 0.5f * width) / height
    val uvY = (offset.y - 0.5f * height) / height

    val camera = floatArrayOf(0f, 0f, -8f, 1f)
    val rayDir = floatArrayOf(uvX, uvY, 1f, 0f)
    val tCamera = FloatArray(4)
    val tRayDir = FloatArray(4)

    Matrix.multiplyMV(tCamera, 0, matrix, 0, camera, 0)
    Matrix.multiplyMV(tRayDir, 0, matrix, 0, rayDir, 0)

    val mag = sqrt(tRayDir[0] * tRayDir[0] + tRayDir[1] * tRayDir[1] + tRayDir[2] * tRayDir[2])
    if (mag == 0f) return null
    tRayDir[0] /= mag; tRayDir[1] /= mag; tRayDir[2] /= mag

    var distanceTraveled = 0f
    for (i in 0 until 64) {
        val px = tCamera[0] + tRayDir[0] * distanceTraveled
        val py = tCamera[1] + tRayDir[1] * distanceTraveled
        val pz = tCamera[2] + tRayDir[2] * distanceTraveled

        val qx = sqrt(px * px + pz * pz) - 2f
        val d = sqrt(qx * qx + py * py) - 1f

        if (d < 0.005f) {
            val uTex = atan2(pz, px) / (2f * PI.toFloat()) + 0.5f
            val vTex = atan2(py, qx) / (2f * PI.toFloat()) + 0.5f
            return Offset(uTex * width, vTex * height)
        }
        distanceTraveled += d
        if (distanceTraveled > 20f) break
    }
    return null
}