# Plano de Implementação e Diffs - Correção da Persistência do Escudo Vetorial (`ClubBadge`)

Diagnóstico e correção do fluxo de persistência do escudo vetorial (`ClubBadge`) no aplicativo **Legacy Master Liga**.

---

## Análise da Causa Raiz

1. **`ClubsViewModel.saveClub()`:** Quando a tela de edição do clube invocava `saveClub`, a função repassava a string JSON do escudo no parâmetro `crestUri`, porém ao chamar `clubRepository.update(clubId, leagueId, normalizedName, crestUri, presidentUserId)` **não fornecia o argumento `badgeConfigJson`**.
2. **`RoomClubRepository.update()`:** O parâmetro opcional `badgeConfigJson` assumia valor `null` por padrão. Durante a cópia da entidade (`current.copy(...)`), a lógica `badgeConfigJson ?: current.badgeConfigJson` resultava em `null`, impedindo que o campo `badgeConfigJson` fosse gravado na tabela `clubs` do banco Room.
3. **`ClubProfileHeaderRow` & `observeProfileHeader`:** A consulta DAO de cabeçalho da tela de perfil do clube não selecionava a nova coluna `badgeConfigJson`, fazendo com que o perfil do clube usasse a decodificação de fallback cinza.

---

## Resumo dos Diffs das Alterações

### 1. `ClubsViewModel.kt`
Garantir a passagem explícita de `badgeConfigJson` em `clubRepository.create` e `clubRepository.update`:

```diff
--- a/app/src/main/java/com/example/legacymasterliga/feature/clubs/presentation/ClubsViewModel.kt
+++ b/app/src/main/java/com/example/legacymasterliga/feature/clubs/presentation/ClubsViewModel.kt
@@ -75,9 +75,23 @@ class ClubsViewModel @Inject constructor(
         viewModelScope.launch {
             runCatching {
                 if (clubId == null) {
-                    clubRepository.create(leagueId, normalizedName, crestUri, presidentUserId, initialBalance)
+                    clubRepository.create(
+                        leagueId = leagueId,
+                        name = normalizedName,
+                        crestUri = crestUri,
+                        presidentUserId = presidentUserId,
+                        initialBalance = initialBalance,
+                        badgeConfigJson = crestUri
+                    )
                 } else {
-                    clubRepository.update(clubId, leagueId, normalizedName, crestUri, presidentUserId)
+                    clubRepository.update(
+                        clubId = clubId,
+                        leagueId = leagueId,
+                        name = normalizedName,
+                        crestUri = crestUri,
+                        presidentUserId = presidentUserId,
+                        badgeConfigJson = crestUri
+                    )
                 }
             }.onSuccess {
```

---

### 2. `RoomClubRepository.kt`
Tratar e persistir o `badgeConfigJson` mesmo quando recebido via parâmetro `crestUri` (se for string em formato JSON):

```diff
--- a/app/src/main/java/com/example/legacymasterliga/data/repository/RoomClubRepository.kt
+++ b/app/src/main/java/com/example/legacymasterliga/data/repository/RoomClubRepository.kt
@@ -107,11 +107,12 @@ class RoomClubRepository @Inject constructor(
             }
         }

+        val resolvedBadgeJson = badgeConfigJson ?: (if (crestUri?.startsWith("{") == true) crestUri else current.badgeConfigJson)

         clubDao.update(
             current.copy(
                 leagueId = leagueId,
                 name = name.trim(),
                 crestUri = crestUri,
-                badgeConfigJson = badgeConfigJson ?: current.badgeConfigJson,
+                badgeConfigJson = resolvedBadgeJson,
                 presidentUserId = presidentUserId,
                 updatedAt = System.currentTimeMillis(),
             ),
```

---

### 3. `ClubProfileHeaderRow.kt`, `ClubProfileModels.kt` e `ClubDao.kt`
Incluir a coluna `badgeConfigJson` na consulta DAO e na model de perfil do clube:

```diff
--- a/app/src/main/java/com/example/legacymasterliga/core/database/model/ClubProfileHeaderRow.kt
+++ b/app/src/main/java/com/example/legacymasterliga/core/database/model/ClubProfileHeaderRow.kt
@@ -5,4 +5,5 @@ data class ClubProfileHeaderRow(
     val clubName: String,
     val crestUri: String?,
+    val badgeConfigJson: String?,
     val presidentUserId: Long?,
     val presidentName: String?,
```

```diff
--- a/app/src/main/java/com/example/legacymasterliga/core/database/dao/ClubDao.kt
+++ b/app/src/main/java/com/example/legacymasterliga/core/database/dao/ClubDao.kt
@@ -32,4 +32,5 @@ interface ClubDao {
                clubs.name AS clubName,
                clubs.crestUri AS crestUri,
+               clubs.badgeConfigJson AS badgeConfigJson,
                clubs.presidentUserId AS presidentUserId,
                users.displayName AS presidentName,
```

```diff
--- a/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/domain/ClubProfileModels.kt
+++ b/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/domain/ClubProfileModels.kt
@@ -6,4 +6,5 @@ data class ClubProfileHeader(
     val clubName: String,
     val crestUri: String?,
+    val badgeConfigJson: String?,
     val presidentUserId: Long?,
```

---

### 4. `ClubProfileScreen.kt`
Passar o `badgeConfigJson` no cabeçalho do perfil do clube para a renderização do `NativeCrest`:

```diff
--- a/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileScreen.kt
+++ b/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileScreen.kt
@@ -816,7 +816,7 @@ private fun ClubHeader(
         Row(
             modifier = Modifier.fillMaxWidth().padding(18.dp),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.spacedBy(16.dp),
         ) {
-            ClubCrest(name, crestUri, Modifier.size(80.dp))
+            ClubCrest(name, crestUri, badgeConfigJson, Modifier.size(80.dp))
```

---

### 5. `OnlineSportsSyncManager.kt`
Mapear e incluir `badgeConfigJson` nas sincronizações do Firestore:

```diff
--- a/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt
+++ b/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt
@@ -530,6 +530,7 @@ class OnlineSportsSyncManager @Inject constructor(
                 common + mapOf(
                     "name" to cl.name,
                     "crestUri" to cl.crestUri,
+                    "badgeConfigJson" to cl.badgeConfigJson,
                     "presidentFirebaseUid" to presidentUid,
                     "isBank" to cl.isBank, "isActive" to cl.isActive, "createdAt" to cl.createdAt,
                 )
@@ -700,6 +701,7 @@ class OnlineSportsSyncManager @Inject constructor(
             id = current?.id ?: 0,
             leagueId = leagueId,
             name = d.string("name"),
             crestUri = d["crestUri"] as? String,
+            badgeConfigJson = d["badgeConfigJson"] as? String ?: (if ((d["crestUri"] as? String)?.startsWith("{") == true) d["crestUri"] as? String else null),
```

---

## Plano de Verificação

### Automated Tests
- Executar testes unitários de `ClubBadgeConfigTest` e compilação do projeto: `gradle_build("app:assembleDebug")`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).
