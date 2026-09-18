# Legacy Master Liga — regras para R8/ProGuard da RC1.
# Room, Hilt, Compose e Navigation utilizam principalmente código gerado em compilação.

# Preserva metadados necessários para anotações, stack traces e injeção.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room: preservar a subclasse concreta do banco e entidades anotadas.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Hilt/Dagger e javax.inject geram implementações em compilação.
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# WorkManager instancia Workers por nome através de sua infraestrutura.
-keep class ** extends androidx.work.ListenableWorker { public <init>(...); }

# Mantém nomes de campos usados nas projeções Room e nos relatórios locais.
-keepclassmembers class com.example.legacymasterliga.core.database.model.** { <fields>; }
