package com.example.chargingstations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider

class MyCameraListener(
    private val context: Context,
    private val placemarkMapObjects: List<PlacemarkMapObject>,
    zoomValue: Float
) : CameraListener {

    val ZOOM_BOUNDARY = 16.4f
    var changed = zoomValue >= ZOOM_BOUNDARY

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        when {
            cameraPosition.zoom >= ZOOM_BOUNDARY && changed -> {
                placemarkMapObjects.forEach {
                    it.setIcon(
                        ImageProvider.fromBitmap(
                            createBitmapFromVector(
                                context,
                                R.drawable.baseline_location_on_24
                            )
                        )
                    )
                }
                changed = false
            }

            cameraPosition.zoom < ZOOM_BOUNDARY && !changed -> {
                placemarkMapObjects.forEach {
                    it.setIcon(
                        ImageProvider.fromBitmap(
                            createBitmapFromVector(
                                context,
                                R.drawable.baseline_circle_24
                            )
                        )
                    )
                }
                changed = true
            }
        }
    }

}

fun createBitmapFromVector(context: Context, art: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, art) ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}