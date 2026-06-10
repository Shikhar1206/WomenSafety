# Add project specific ProGuard rules here.

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { *; }

# ── Room Database ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao interface * { *; }
-keep interface * extends androidx.room.RoomDatabase { *; }

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Hilt ──────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# ── Data Classes (keep for Firestore serialization) ───────────────────────────
-keepclassmembers class com.example.womensafety.data.** { *; }
-keepclassmembers class com.example.womensafety.domain.model.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ── Timber ────────────────────────────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**

# ── Keep serializable classes ─────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}