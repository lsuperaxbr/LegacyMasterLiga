# Plano de Implementação e Diffs - Correção do Mapeamento de Position e Foot em `Pes6PlayerTextParser.kt`

Diagnóstico e solução para que os campos **Posição (`position`)** e **Pé Dominante (`preferredFoot`)** sejam convertidos e preenchidos automaticamente na ficha do jogador durante a importação do elenco por pasta.

---

## Diagnóstico da Causa Raiz

1. **Acentuação e Chaves Múltiplas:** A função de normalização de chaves não removia caracteres acentuados (ex: `"Posição"` $\rightarrow$ `"posição"`), impedindo a localização da chave no `ALIAS_MAP`.
2. **Ausência de Mapeamento de Valores Brutos:** O parser gravava o valor textual bruto do arquivo (ex: `"CB"`, `"Zagueiro"`, `"Right"`, `"Destro"`). Como a interface do app aguarda siglas padronizadas da Master Liga (`"ZAG"`, `"GOL"`, `"CA"`, `"R"`, `"L"`), os valores brutos não eram reconhecidos e a UI exibia "Posição" em branco e "Não informado".

---

## Mapeamento de Valores Proposto

### A. Mapeamento de Posição (`convertPosition`)
- **`GOL`**: `GK`, `GOL`, `GOLEIRO`
- **`ZAG`**: `CB`, `ZAG`, `ZAGUEIRO`, `DF`, `SW`, `LAT`, `SB`
- **`LD`**: `LD`, `RB`, `LATERAL DIREITO`, `LATERAL-DIREITO`
- **`LE`**: `LE`, `LB`, `LATERAL ESQUERDO`, `LATERAL-ESQUERDO`
- **`VOL`**: `VOL`, `DM`, `DMC`, `VOLANTE`
- **`MC`**: `MC`, `CM`, `MEIO`, `MEIO CAMPO`
- **`MEI`**: `MEI`, `AM`, `AMC`, `MAT`, `CAM`, `MEIA ATACANTE`
- **`MD`**: `MD`, `RM`, `MEIA DIREITA`
- **`ME`**: `ME`, `LM`, `MEIA ESQUERDA`
- **`PD`**: `PD`, `RW`, `RWF`, `PONTA DIREITA`
- **`PE`**: `PE`, `LW`, `LWF`, `PONTA ESQUERDA`
- **`SA`**: `SA`, `SS`, `SEGUNDO ATACANTE`
- **`CA`**: `CA`, `ST`, `CF`, `ATA`, `ATACANTE`, `FW`, `CENTROAVANTE`
- Ausente ou não reconhecido: `null`

### B. Mapeamento de Pé Dominante (`convertFoot`)
- **`R`**: `R`, `RIGHT`, `DIREITO`, `D`, `DESTRO`
- **`L`**: `L`, `LEFT`, `CANHOTO`, `ESQUERDO`, `E`
- Ausente ou não reconhecido: `null`

---

## Diffs Propostos para `Pes6PlayerTextParser.kt`

```diff
--- a/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/Pes6PlayerTextParser.kt
+++ b/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/Pes6PlayerTextParser.kt
@@ -29,8 +29,10 @@ object Pes6PlayerTextParser {

     val ALIAS_MAP = mapOf(
         // Header info
         "name" to "NAME", "playername" to "NAME",
-        "position" to "POSITION", "pos" to "POSITION",
+        "position" to "POSITION", "pos" to "POSITION", "posicao" to "POSITION",
         "ovr" to "OVR", "overall" to "OVR",
         "age" to "AGE", "idade" to "AGE",
-        "foot" to "FOOT", "pe" to "FOOT", "pedominante" to "FOOT", "preferredfoot" to "FOOT",
+        "foot" to "FOOT", "pe" to "FOOT", "pedominante" to "FOOT", "preferredfoot" to "FOOT",
         "height" to "HEIGHT", "altura" to "HEIGHT", "alturacm" to "HEIGHT", "heightcm" to "HEIGHT",

     private fun normalizeKey(key: String): String {
-        return key.lowercase().replace("_", "").replace(" ", "").trim()
+        return key.lowercase()
+            .replace("_", "").replace(" ", "").replace("-", "")
+            .replace("ã", "a").replace("á", "a").replace("â", "a")
+            .replace("é", "e").replace("ê", "e").replace("í", "i")
+            .replace("ó", "o").replace("õ", "o").replace("ú", "u")
+            .replace("ç", "c").trim()
     }

+    fun convertPosition(raw: String?): String? {
+        val clean = raw?.trim()?.uppercase() ?: return null
+        if (clean.isBlank()) return null
+        return when {
+            clean in listOf("GOL", "GK", "GOLEIRO") -> "GOL"
+            clean in listOf("ZAG", "CB", "ZAGUEIRO", "DF", "SW", "LAT", "SB") -> "ZAG"
+            clean in listOf("LD", "RB", "LATERAL DIREITO", "LATERAL-DIREITO") -> "LD"
+            clean in listOf("LE", "LB", "LATERAL ESQUERDO", "LATERAL-ESQUERDO") -> "LE"
+            clean in listOf("VOL", "DM", "DMC", "VOLANTE") -> "VOL"
+            clean in listOf("MC", "CM", "MEIO", "MEIO CAMPO", "MEIO-CAMPO") -> "MC"
+            clean in listOf("MEI", "AM", "AMC", "MAT", "CAM", "MEIA ATACANTE") -> "MEI"
+            clean in listOf("MD", "RM", "MEIA DIREITA") -> "MD"
+            clean in listOf("ME", "LM", "MEIA ESQUERDA") -> "ME"
+            clean in listOf("PD", "RW", "RWF", "PONTA DIREITA") -> "PD"
+            clean in listOf("PE", "LW", "LWF", "PONTA ESQUERDA") -> "PE"
+            clean in listOf("SA", "SS", "SEGUNDO ATACANTE") -> "SA"
+            clean in listOf("CA", "ST", "CF", "ATA", "ATACANTE", "FW", "CENTROAVANTE") -> "CA"
+            else -> if (clean in setOf("GOL", "ZAG", "LD", "LE", "VOL", "MC", "MEI", "MD", "ME", "PD", "PE", "SA", "CA")) clean else null
+        }
+    }

+    fun convertFoot(raw: String?): String? {
+        val clean = raw?.trim()?.uppercase() ?: return null
+        if (clean.isBlank()) return null
+        return when {
+            clean in listOf("R", "RIGHT", "DIREITO", "D", "DESTRO") || clean.startsWith("D") || clean.startsWith("R") -> "R"
+            clean in listOf("L", "LEFT", "CANHOTO", "ESQUERDO", "E") || clean.startsWith("L") || clean.startsWith("E") || clean.startsWith("C") -> "L"
+            else -> null
+        }
+    }
```

---

## Plano de Verificação

### Automated Tests
1. **`ImportSquadFromFolderUseCaseTest.kt`:**
   - Adicionar testes cobrindo:
     - `Position`: `"ZAG"` $\rightarrow$ `"ZAG"`, `"CB"` $\rightarrow$ `"ZAG"`, `"GK"` $\rightarrow$ `"GOL"`, `"ST"` $\rightarrow$ `"CA"`.
     - `Foot`: `"R"` $\rightarrow$ `"R"`, `"Destro"` $\rightarrow$ `"R"`, `"L"` $\rightarrow$ `"L"`, `"Canhoto"` $\rightarrow$ `"L"`.
     - Ausências de `Position` e `Foot` resultando em `null`.
2. Executar compilação via Gradle: `gradle_build("app:assembleDebug")`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).
