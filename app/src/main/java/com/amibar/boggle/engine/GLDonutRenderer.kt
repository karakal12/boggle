package com.amibar.boggle.engine

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import org.intellij.lang.annotations.Language
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A GLSurfaceView.Renderer that renders a rotating 3D torus (donut) using OpenGL ES 2.0.
 *
 * This renderer uses a unique approach: instead of pre-calculating 3D vertex positions on the CPU,
 * it sends a grid of (theta, phi) coordinates (UVs) to the GPU. The vertex shader then
 * calculates the 3D position of each vertex in real-time using the parametric equations
 * of a torus. This significantly reduces the amount of data transferred to the GPU per frame.
 */
class GLDonutRenderer : GLSurfaceView.Renderer {

    // OpenGL Program ID
    private var program: Int = 0
    
    // Buffers for storing vertex data on the GPU
    private lateinit var uvBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer

    // Projection matrix to handle perspective and aspect ratio
    private val projectionMatrix = FloatArray(16)

    // Rotation angles for the three axes (A=X, B=Y, Z=Z)
    @Volatile
    var angleA: Float = 0f
    @Volatile
    var angleB: Float = 0f
    @Volatile
    var angleZ: Float = 0f
    
    // Distance of the donut from the camera
    @Volatile
    var donutDistance: Float = 10f

    /** Updates rotation angle around the X-axis */
    fun addA(v: Float) { angleA += v }
    /** Updates rotation angle around the Y-axis */
    fun addB(v: Float) { angleB += v }
    /** Updates rotation angle around the Z-axis */
    fun addZ(v: Float) { angleZ += v }
    /** Scales the distance of the donut from the camera */
    fun scaleDonutDistance(v: Float) { donutDistance *= v }

    private var indexCount: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set background color to a dark gray
        GLES20.glClearColor(0.26f, 0.26f, 0.26f, 1.0f)
        // Enable depth testing to correctly render overlapping surfaces
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Compile and link the shaders into a program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // Initialize the UV grid that defines the torus topology
        initTemplateGrid()
    }

    /**
     * Generates a grid of (theta, phi) values that cover the entire surface of the torus.
     * theta (u) goes around the cross-section of the tube (0 to 2pi).
     * phi (v) goes around the central axis of the donut (0 to 2pi).
     */
    private fun initTemplateGrid() {
        val thetaSteps = 64 // Number of points around the tube
        val phiSteps = 90   // Number of points around the main ring
        val uvs = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        // 1. Generate the grid of (theta, phi) parameters
        for (i in 0 until thetaSteps) {
            val u = (i * 2.0 * Math.PI / thetaSteps).toFloat()
            for (j in 0 until phiSteps) {
                val v = (j * 2.0 * Math.PI / phiSteps).toFloat()
                uvs.add(u); uvs.add(v)
            }
        }

        // 2. Generate indices for drawing triangles (two per grid cell)
        for (i in 0 until thetaSteps) {
            for (j in 0 until phiSteps) {
                val nextI = (i + 1) % thetaSteps
                val nextJ = (j + 1) % phiSteps
                
                // Four corners of the current grid cell
                val p1 = (i * phiSteps + j).toShort()
                val p2 = (nextI * phiSteps + j).toShort()
                val p3 = (i * phiSteps + nextJ).toShort()
                val p4 = (nextI * phiSteps + nextJ).toShort()
                
                // First triangle: p1-p2-p4
                indices.add(p1); indices.add(p2); indices.add(p4)
                // Second triangle: p1-p4-p3
                indices.add(p1); indices.add(p4); indices.add(p3)
            }
        }

        indexCount = indices.size
        
        // 3. Move data into direct ByteBuffers for OpenGL
        uvBuffer = ByteBuffer.allocateDirect(uvs.size * 4).run {
            order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(uvs.toFloatArray()).position(0)
            }
        }
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder()).asShortBuffer().apply {
                put(indices.toShortArray()).position(0)
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Update viewport to match screen dimensions
        GLES20.glViewport(0, 0, width, height)
        
        // Calculate the perspective projection matrix
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear color and depth buffers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // Use our shader program
        GLES20.glUseProgram(program)

        // Pass uniforms (rotation angles, distance, projection) to the GPU
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uA"), angleA)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uB"), angleB)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uC"), angleZ)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uDistance"), donutDistance)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uProjection"), 1, false, projectionMatrix, 0)

        // Pass the UV attribute data
        val uvHandle = GLES20.glGetAttribLocation(program, "aUV")
        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 8, uvBuffer)

        // Draw the donut using indexed triangles
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Clean up
        GLES20.glDisableVertexAttribArray(uvHandle)
    }

    /** Helper function to compile a shader */
    private fun loadShader(type: Int, shaderCode: String) = GLES20.glCreateShader(type).also {
        GLES20.glShaderSource(it, shaderCode)
        GLES20.glCompileShader(it)
    }

    companion object {
        @Language("GLSL")
        private val vertexShaderCode = """
            uniform mat4 uProjection;
            uniform float uA, uB, uC, uDistance;
            attribute vec2 aUV; // x = theta (tube angle), y = phi (main ring angle)
            varying vec3 vColor;
            varying float vLight;
            
            void main() {
                float theta = aUV.x;
                float phi = aUV.y;
                
                // Torus parameters: R1 is the radius of the tube, R2 is the distance from the center to the tube center.
                float R1 = 1.0;
                float R2 = 2.0;
                
                // 1. Calculate torus position in local space using parametric equations
                // x = (R2 + R1*cos(theta)) * cos(phi)
                // y = (R2 + R1*cos(theta)) * sin(phi)
                // z = R1*sin(theta)
                float circleX = R2 + R1 * cos(theta);
                float circleY = R1 * sin(theta);
                vec3 pos = vec3(circleX * cos(phi), circleX * sin(phi), circleY);
                
                // 2. Calculate normal in local space (used for lighting)
                // The normal is simply the vector from the tube's center to the surface point.
                vec3 norm = vec3(cos(theta) * cos(phi), cos(theta) * sin(phi), sin(theta));
                
                // 3. Rotation Matrices (constructed on GPU)
                // Rotation around X axis (uA)
                mat3 rotX = mat3(
                    1.0, 0.0, 0.0,
                    0.0, cos(uA), sin(uA),
                    0.0, -sin(uA), cos(uA)
                );
                // Rotation around Y axis (uB)
                mat3 rotY = mat3(
                    cos(uB), 0.0, -sin(uB),
                    0.0, 1.0, 0.0,
                    sin(uB), 0.0, cos(uB)
                );
                // Rotation around Z axis (uC)
                mat3 rotZ = mat3(
                    cos(uC), sin(uC), 0.0,
                    -sin(uC), cos(uC), 0.0,
                    0.0, 0.0, 1.0
                );
                
                // Apply rotations to position and normal
                pos = rotX * rotY * rotZ * pos;
                norm = rotX * rotY * rotZ * norm;
                
                // 4. Transform to View Space
                // Move the donut back by uDistance so it's visible by the camera
                vec4 viewPos = vec4(pos.x, pos.y, pos.z - uDistance, 1.0);
                
                // 5. Transform to Clip Space
                gl_Position = uProjection * viewPos;
                
                // 6. Simple Lighting
                // Light comes from (0, 1, 1) in view space
                vec3 lightDir = normalize(vec3(0.0, 1.0, 1.0));
                vLight = max(dot(norm, lightDir), 0.0) + 0.2; // Ambient + Diffuse
                
                // 7. Dynamic Color calculation
                // Base colors are mixed based on the phi angle (rotation around the donut hole)
                vColor = mix(vec3(1.0, 0.4, 0.4), vec3(0.4, 0.4, 1.0), 0.5 + 0.5 * sin(phi));
                
                // Adjust saturation based on theta (distance from the "top" of the tube)
                float sat = abs(theta - 3.14159) / 3.14159;
                vColor = mix(vec3(1.0), vColor, sat);
            }
        """.trimIndent()

        @Language("GLSL")
        private val fragmentShaderCode = """
            precision mediump float;
            varying vec3 vColor;
            varying float vLight;
            void main() {
                // Combine surface color with calculated lighting intensity
                gl_FragColor = vec4(vColor * vLight, 1.0);
            }
        """.trimIndent()
        
        // Default rotation rates
        const val A_RATE = 0.005f
        const val B_RATE = 0.007f
    }
}
