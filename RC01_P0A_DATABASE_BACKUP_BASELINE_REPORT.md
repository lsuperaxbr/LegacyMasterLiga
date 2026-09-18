# RC01-P0A — Database, backup and reliable build baseline

Date: 2026-08-03  
Reference: `PROJECT_AUDIT_RC01.md`  
Scope status: **implementation completed; build baseline not approved in this environment**.

No Copa, screen, login, Market, Finance or Competition behavior was intentionally changed. RC01-P0B was not started.

## 1. Database version found

Direct inspection before the change confirmed:

- `AppDatabase`: version **19**;
- `exportSchema`: already `true`;
- last declared migration: **18→19**;
- migrations available and registered: every consecutive step from **1→2** through **18→19**;
- `RoomBackupRepository`: hard-coded version **14**;
- `FirebaseConnectionChecker`: hard-coded diagnostic version **15**;
- no use of `fallbackToDestructiveMigration` in the project.

## 2. Official version after the change

The official database version remains **19**. No schema change or new migration was necessary.

`DatabaseContract.VERSION` is now the single source of truth and is consumed by:

- the `@Database` annotation;
- backup manifest creation;
- backup compatibility validation;
- restore compatibility validation;
- Firebase diagnostic logging.

`DatabaseContract.NAME` also centralizes the physical database name. The duplicated production numbers 14 and 15 were removed.

## 3. Backup and restore

### Changes implemented

- new backups declare database v19 from the official contract;
- package validation still verifies ZIP format, SQLite header and SHA-256;
- validation now also reads `PRAGMA user_version` directly from the extracted SQLite file;
- manifest version and real SQLite version must match;
- versions greater than v19 are rejected with a clear message;
- invalid/unsupported versions are rejected;
- older versions from the supported migration chain are accepted and explicitly marked for Room migration after restart;
- restore closes the injected Room instance and verifies `database.isOpen == false` before replacing the file;
- WAL and SHM sidecars are removed only after Room is closed;
- the existing safety backup remains mandatory before replacement;
- the existing restart flag remains mandatory, so Hilt/Room/DAOs are recreated in a fresh process after restore;
- no destructive migration or data deletion fallback was introduced.

### Tests added

`BackupVersionPolicyTest` covers:

1. current version accepted;
2. recent older version accepted for migration;
3. newer version rejected clearly;
4. mismatch between manifest and SQLite rejected.

`BackupRestoreIntegrationTest` covers:

1. backup creation at the official version;
2. reading the manifest header;
3. restoring data;
4. confirmation that the old Room instance is closed;
5. restart flag creation;
6. reopening Room and confirming restored data while later data is absent.

### Execution result

The backup tests were **compiled neither executed nor approved**, because Gradle failed during generated dependency-accessor configuration before reaching Kotlin compilation or test discovery. They remain pending execution in Android Studio or a normal terminal outside the restricted filesystem monitor.

Static verification completed:

- production backup version now references `DatabaseContract.VERSION`;
- no production hard-coded database versions 14, 15 or a second 19 remain;
- the restore path contains no `fallbackToDestructiveMigration`;
- the old Room instance is explicitly closed and checked before file replacement.

## 4. Migrations and schemas

### Explicit migration chain

There are 18 explicit, consecutive migrations:

`1→2`, `2→3`, `3→4`, `4→5`, `5→6`, `6→7`, `7→8`, `8→9`, `9→10`, `10→11`, `11→12`, `12→13`, `13→14`, `14→15`, `15→16`, `16→17`, `17→18`, `18→19`.

They are now exposed through one ordered registry, `DatabaseModule.migrations`, and the Room builder registers that array. `MigrationRegistryTest` verifies:

- first supported source version;
- continuity between every pair;
- arrival at the official current version;
- expected number of steps.

The existing `OnlineSyncMigrationTest` covers preservation of data and creation of the two v19 sync tables for 18→19.

### Exported schemas available

- `6.json`;
- `7.json`;
- `12.json`;
- `13.json`;
- `14.json`;
- `15.json`;
- `16.json`;
- `17.json`;
- `18.json`;
- `19.json`.

The current `19.json` already exists. Its SHA-256 during this audit was:

`5FE3F3013EB7213B6B614329FB99B222E31E71CA7640A7CD1E01E1F6CEE59B80`

Schemas 1–5 and 8–11 remain unavailable. No historical schema was fabricated.

Strategy for future releases:

1. keep `exportSchema = true`;
2. commit every newly generated schema with the release;
3. append each migration to `DatabaseModule.migrations`;
4. add a migration test from the immediately previous committed schema;
5. never recreate a missing historical schema by guessing its structure.

## 5. Firebase diagnostic

The write to `system_status/connection_test` was removed.

The checker now:

- uses the injected `FirebaseAuth` and `FirebaseFirestore` instances;
- reports whether Firebase is initialized;
- reports whether a user is authenticated;
- performs no remote request when unauthenticated;
- when authenticated, performs only a server read from the existing `system_status/connection_test` document;
- distinguishes successful Firestore access, `PERMISSION_DENIED`, `UNAVAILABLE`/no internet and unexpected errors;
- logs the official database version through `DatabaseContract.VERSION`;
- no longer reads or transmits `ANDROID_ID`;
- does not modify rules, profiles, leagues, members or invites.

This is only an infrastructure diagnostic. It does **not** prove that Liga Online works.

## 6. Build baseline

### Command 1

`:app:testDebugUnitTest`

Result: **FAILED BEFORE TEST EXECUTION**.

The failure occurred while Gradle attempted to compile generated version-catalog accessors:

```text
GeneratedClassCompilationException: Unable to compile generated classes
Caused by: java.nio.file.AccessDeniedException: ...gradle-logging-9.5.0.jar
```

A second isolated attempt using a copied Gradle runtime failed identically on `gradle-base-services-9.5.0.jar`. This confirms a filesystem/JDK ZIP handling restriction in the execution environment, before project Kotlin compilation.

Test accounting:

- unit test methods declared in `app/src/test`: **36**;
- tests reached/executed by this run: **0**;
- tests approved: **0**;
- assertion failures: **0**;
- infrastructure/configuration failures: **1**;
- result: **not approved**.

The count of 36 is a source inventory, not a successful Gradle test report.

### Command 2

`:app:assembleDebug`

Result: **FAILED BEFORE MODULE COMPILATION**, with the same `GeneratedClassCompilationException` and `AccessDeniedException` while closing a Gradle JAR.

Warnings/notes:

- Gradle reported creation of a single-use daemon because of JVM settings;
- no Kotlin, Android lint or application warning phase was reached;
- therefore there are no trustworthy code-warning results for this baseline.

### APK

No new APK was generated or approved.

A pre-existing file remains at `app/build/outputs/apk/debug/app-debug.apk`, timestamped **2026-08-02 16:00:57**. It predates RC01-P0A and must not be distributed as the output of this change.

## 7. Files changed

Production:

- `app/src/main/java/com/example/legacymasterliga/core/database/AppDatabase.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/di/DatabaseModule.kt`;
- `app/src/main/java/com/example/legacymasterliga/feature/backup/data/BackupVersionPolicy.kt`;
- `app/src/main/java/com/example/legacymasterliga/feature/backup/data/RoomBackupRepository.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/network/FirebaseConnectionChecker.kt`;
- `app/src/main/java/com/example/legacymasterliga/LegacyMasterLigaApp.kt`.

Tests:

- `app/src/test/java/com/example/legacymasterliga/core/database/MigrationRegistryTest.kt`;
- `app/src/test/java/com/example/legacymasterliga/feature/backup/data/BackupVersionPolicyTest.kt`;
- `app/src/test/java/com/example/legacymasterliga/feature/backup/data/BackupRestoreIntegrationTest.kt`.

Report:

- `RC01_P0A_DATABASE_BACKUP_BASELINE_REPORT.md`.

## 8. Remaining limitations and release decision

RC01-P0A cannot yet be called a reliable build baseline because neither mandatory Gradle command reached project compilation.

Required external validation:

1. run `gradlew.bat :app:testDebugUnitTest` in Android Studio or a normal unrestricted terminal;
2. confirm all 36 declared unit tests are discovered and pass;
3. run `gradlew.bat :app:assembleDebug` only after the test task is green;
4. record warnings and the newly generated APK path/timestamp/hash;
5. run the backup/restore integration scenario and confirm the application restarts with a newly created Room graph;
6. test restoration of a genuine v18 backup into the v19 APK.

Final decision for this environment: **code candidate produced; RC01-P0A baseline REPROVED/PENDING due to build-environment failure. No APK released.**

Work stops here. RC01-P0B, Copa and login were not started.
