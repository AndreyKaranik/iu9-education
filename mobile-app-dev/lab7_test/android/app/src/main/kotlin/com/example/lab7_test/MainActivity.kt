package com.example.lab7_test

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import com.yandex.mapkit.MapKitFactory
class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        MapKitFactory.setApiKey("83258943-5e6f-4d87-b448-45553225d7e4") // Your generated API key
        super.configureFlutterEngine(flutterEngine)
    }
}
