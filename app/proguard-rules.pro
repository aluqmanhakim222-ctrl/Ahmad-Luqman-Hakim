# Keep WebView JavaScript interface methods
-keepclassmembers class com.example.ui.classroom.ClassroomJsBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
