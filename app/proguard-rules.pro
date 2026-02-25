# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Widget ────────────────────────────────────────────────────────────────────

# Keep all widget classes — R8 would otherwise strip/rename them and the
# home screen launcher cannot instantiate the receiver or GlanceAppWidget.
-keep class com.example.bullet.widget.** { *; }

# Keep Glance base classes referenced by the widget
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# ── Hilt EntryPoints ─────────────────────────────────────────────────────────

# EntryPointAccessors.fromApplication() looks up the interface by class
# reference at runtime — renaming it breaks the lookup.
-keep @dagger.hilt.EntryPoint interface * { *; }

# ── Room / Database ───────────────────────────────────────────────────────────

# Keep enum constant NAMES (not just the classes). Converters.kt stores enum
# values in the DB as their .name() string and reads them back with .valueOf().
# If R8 renames TASK → a, BulletType.valueOf("TASK") throws at runtime in
# release builds even though debug builds work fine (no obfuscation there).
-keep enum com.example.bullet.data.db.** { *; }

# Keep the TypeConverter class and its methods — Room's generated code calls
# these directly; renaming breaks the DB layer used by the widget.
-keep class com.example.bullet.data.db.Converters { *; }

# Keep Room entity field names — they map to DB column names via annotations.
-keep @androidx.room.Entity class * { *; }