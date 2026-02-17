package com.amibar.boggle.utils

import org.apache.commons.math3.complex.Quaternion
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import kotlin.math.sqrt

/**
 * Rotates a vector by a quaternion.
 */
fun rotate(v: Vector3D, q: Quaternion): Vector3D {
    val x = v.x
    val y = v.y
    val z = v.z
    val q0 = q.q0
    val q1 = q.q1
    val q2 = q.q2
    val q3 = q.q3
    
    val rx = (1.0 - 2.0 * q2 * q2 - 2.0 * q3 * q3) * x + (2.0 * q1 * q2 - 2.0 * q0 * q3) * y + (2.0 * q1 * q3 + 2.0 * q0 * q2) * z
    val ry = (2.0 * q1 * q2 + 2.0 * q0 * q3) * x + (1.0 - 2.0 * q1 * q1 - 2.0 * q3 * q3) * y + (2.0 * q2 * q3 - 2.0 * q0 * q1) * z
    val rz = (2.0 * q1 * q3 - 2.0 * q0 * q2) * x + (2.0 * q2 * q3 + 2.0 * q0 * q1) * y + (1.0 - 2.0 * q1 * q1 - 2.0 * q2 * q2) * z
    return Vector3D(rx, ry, rz)
}

/**
 * Optimized torus intersection using Ray Marching (SDF).
 * This is significantly faster than solving a quartic equation analytically.
 */
fun rayIntersectTorus(
    rayOrigin: Vector3D,
    rayDirection: Vector3D,
    qTorus: Quaternion,
    majorRadius: Double,
    minorRadius: Double
): Double {
    val qInv = qTorus.getInverse()
    val localOrigin = rotate(rayOrigin, qInv)
    val localDirection = rotate(rayDirection, qInv)
    
    val originX = localOrigin.x
    val originY = localOrigin.y
    val originZ = localOrigin.z
    val directionX = localDirection.x
    val directionY = localDirection.y
    val directionZ = localDirection.z

    var t = 0.0
    // For a torus centered at origin, we march along the ray
    for (i in 0 until 64) {
        val pointX = originX + t * directionX
        val pointY = originY + t * directionY
        val pointZ = originZ + t * directionZ
        
        // Torus Distance Function
        val distXY = sqrt(pointX * pointX + pointY * pointY)
        val dist = sqrt((distXY - majorRadius) * (distXY - majorRadius) + pointZ * pointZ) - minorRadius
        
        if (dist < 0.001) return t
        t += dist
        if (t > 200.0) break
    }
    return Double.POSITIVE_INFINITY
}

/**
 * Calculates the normal vector at a point on the torus surface.
 */
fun getTorusNormal(
    pointWorld: Vector3D,
    torusRotation: Quaternion,
    majorRadius: Double
): Vector3D {
    val invRot = torusRotation.getInverse()
    val localPoint = rotate(pointWorld, invRot)
    
    val distXY = sqrt(localPoint.x * localPoint.x + localPoint.y * localPoint.y)
    val normalX: Double
    val normalY: Double
    val normalZ: Double
    
    if (distXY < 1e-6) {
        normalX = 0.0
        normalY = 0.0
        normalZ = if (localPoint.z > 0) 1.0 else -1.0
    } else {
        val k = 1.0 - majorRadius / distXY
        val vx = localPoint.x * k
        val vy = localPoint.y * k
        val vz = localPoint.z
        val len = sqrt(vx * vx + vy * vy + vz * vz)
        normalX = vx / len
        normalY = vy / len
        normalZ = vz / len
    }
    
    return rotate(Vector3D(normalX, normalY, normalZ), torusRotation)
}
