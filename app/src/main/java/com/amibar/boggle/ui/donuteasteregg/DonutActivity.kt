package com.amibar.boggle.ui.donuteasteregg

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amibar.boggle.engine.GLDonutRenderer

/**
 * An Activity that displays a 3D rotating donut (torus) using Jetpack Compose.
 * This activity hosts a GLSurfaceView and handles touch gestures for interaction.
 */
class DonutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DonutScreen()
        }
    }
}

@Composable
fun DonutScreen() {
    val renderer = remember { GLDonutRenderer() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep a reference to the GLSurfaceView to handle its lifecycle (onResume/onPause)
    var glSurfaceView by remember { mutableStateOf<GLSurfaceView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glSurfaceView?.onResume()
                Lifecycle.Event.ON_PAUSE -> glSurfaceView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    // Update rotation angles in the renderer
                    // dx and dy correspond to pan.x and pan.y
                    renderer.addA(pan.x * GLDonutRenderer.A_RATE)
                    renderer.addB(pan.y * GLDonutRenderer.B_RATE)

                    // Update zoom (distance)
                    // detector.scaleFactor in original code corresponds to zoom in Compose
                    renderer.scaleDonutDistance(1 / zoom)

                    // Update rotation around Z axis
                    // Compose provides rotation in degrees, original used radians
                    renderer.addZ(rotation * (Math.PI.toFloat() / 180f))
                }
            },
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                setRenderer(renderer)
                glSurfaceView = this
            }
        },
        update = {
            // No specific updates needed here
        }
    )
}

@Composable
fun AgslDonut(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
    ) {

    }
}
