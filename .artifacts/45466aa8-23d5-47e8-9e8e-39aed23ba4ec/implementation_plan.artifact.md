# Plano de Implementação - Sistema Vetorial de Escudos (`ClubBadge`)

Evolução do editor e sistema de renderização de escudos do aplicativo **Legacy Master Liga**, substituindo a lógica atual por um motor vetorial completo baseado em Compose Canvas, formas geométricas, padrões, paleta de cores e símbolos centrais (`ClubBadgeConfig`).

---

## Estrutura do Recurso e Regras de Negócio

### 1. Modelo de Dados (`ClubBadgeConfig.kt`)
Data class para representar a configuração do escudo:
```kotlin
data class ClubBadgeConfig(
    val shape: String = "shield",          // "shield", "circle", "diamond", "hexagon", "crest"
    val pattern: String = "solid",         // "solid", "vertical_stripes", "horizontal_stripes", "diagonal_stripes", "half", "cross", "central_circle"
    val primaryColor: String = "#808080",  // HEX
    val secondaryColor: String = "#FFFFFF",// HEX
    val accentColor: String = "#000000",   // HEX
    val symbol: String = "initial"         // "initial", "star", "crown", "lightning", "ball", "tree", "none"
)
```
- **Codificação/Decodificação:** Métodos para converter de/para string formatada no campo `crestUri` da tabela de clubes (garantindo compatibilidade com o banco de dados Room sem necessidade de alterar schemas SQL).
- **Valores Padrão para Clubes Existentes:** `shape = "shield"`, `pattern = "solid"`, `primaryColor = "#808080"`, `secondaryColor = "#FFFFFF"`, `accentColor = "#000000"`, `symbol = "initial"`.

---

### 2. Componentes Vetoriais de Desenho (Canvas)

- **`BadgeShapeDrawer.kt`:**
  - Gera paths fechados para as 5 formas geométricas suportadas:
    - `shield` (Escudo clássico curvado)
    - `circle` (Círculo perfeito)
    - `diamond` (Losango)
    - `hexagon` (Hexágono regular)
    - `crest` (Escudo heráldico tradicional com topo reto)

- **`BadgePatternDrawer.kt`:**
  - Aplica padrões visuais recortados (`clipPath`) dentro da forma com a `secondaryColor`:
    - `solid` (Preenchimento uniforme)
    - `vertical_stripes` (Listras verticais alternadas)
    - `horizontal_stripes` (Listras horizontais alternadas)
    - `diagonal_stripes` (Listras diagonais)
    - `half` (Bipartido meio a meio)
    - `cross` (Cruz centrada)
    - `central_circle` (Círculo central interno)

- **`BadgeSymbolDrawer.kt`:**
  - Desenha os símbolos centrais em `accentColor`:
    - `initial` (Inicial do nome do clube)
    - `star` (Estrela de 5 pontas)
    - `crown` (Coroa)
    - `lightning` (Raio/Relâmpago)
    - `ball` (Bola de futebol vetorial)
    - `tree` (Árvore/Pinho)
    - `none` (Nenhum símbolo)

- **`ClubBadge.kt`:**
  - Composable principal que junta a forma base preenchida com `primaryColor`, o padrão recortado em `secondaryColor`, o símbolo central em `accentColor` e a borda externa sutil para definição.

---

### 3. Editor de Escudo na Tela de Edição (`ClubsScreen.kt`)
Substituição do editor em `ClubEditorDialog` pelos novos controles interativos com **Preview em Tempo Real**:
- **Forma do Escudo:** Chips/Radio com rótulos e seletores para as 5 formas.
- **Padrão:** Chips/Radio para os 7 padrões de preenchimento.
- **Cores:** Paletas compactas de seleção para Cor Primária, Cor Secundária e Cor de Destaque.
- **Símbolo Central:** Chips/Radio para os 7 símbolos suportados.

---

### 4. Padronização nos Tamanhos do App
Atualização do `NativeCrest.kt` / `ClubBadge` para renderização em todos os pontos do app com dimensões padronizadas:
- **Perfil do clube (`ClubProfileScreen`):** `80.dp`
- **Lista de clubes (`ClubsScreen`):** `40.dp`
- **Jogos / Rodada / Partidas (`ScheduleScreen` / `Dashboard`):** `32.dp`
- **Tabela de Classificação (`StandingsScreen`):** `24.dp`

---

## Proposta de Mudanças Arquivos por Arquivo

### Domain & UI Components

#### [NEW] [ClubBadgeConfig.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/model/ClubBadgeConfig.kt)
- Classe de dados, valores padrão e funções de codificação/decodificação para o campo `crestUri`.

#### [NEW] [BadgeShapeDrawer.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/ui/components/badge/BadgeShapeDrawer.kt)
- Desenhador vetorial das formas geométricas (`shield`, `circle`, `diamond`, `hexagon`, `crest`).

#### [NEW] [BadgePatternDrawer.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/ui/components/badge/BadgePatternDrawer.kt)
- Desenhador dos 7 padrões visuais com clipping na forma.

#### [NEW] [BadgeSymbolDrawer.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/ui/components/badge/BadgeSymbolDrawer.kt)
- Desenhador dos símbolos centrais vetoriais (`initial`, `star`, `crown`, `lightning`, `ball`, `tree`, `none`).

#### [NEW] [ClubBadge.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/ui/components/badge/ClubBadge.kt)
- Composable principal do escudo vetorial.

#### [MODIFY] [NativeCrest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/ui/components/NativeCrest.kt)
- Atualizar para integrar e delegar o desenho para o novo `ClubBadge`.

#### [MODIFY] [ClubsScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/clubs/presentation/ClubsScreen.kt)
- Atualizar o diálogo de edição do clube (`ClubEditorDialog`) com o novo editor de forma, padrão, cores e símbolo com preview em tempo real.

---

## Plano de Verificação

### Automated Tests
- Criar `ClubBadgeConfigTest.kt` para validar a codificação/decodificação e tratamento dos valores de fallback padrão.
- Executar os testes unitários via Gradle: `gradle_build("app:assembleDebug")`

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).
