# Walkthrough - Sistema Vetorial de Escudos (`ClubBadge`)

Evolução do sistema de escudos do aplicativo **Legacy Master Liga**, substituindo o renderizador antigo por um motor vetorial leve baseado em Jetpack Compose Canvas, com suporte a 5 formas geométricas, 7 padrões visuais, paletas de cores, 7 símbolos centrais (incluindo a 1ª letra maiúscula do clube) e armazenamento em JSON na coluna `badgeConfigJson`.

---

## O Que Foi Criado e Atualizado

### 1. Modelo de Dados e Banco Room (`ClubBadgeConfig.kt`, `AppDatabase.kt` & `DatabaseModule.kt`)
- **`ClubBadgeConfig`:** Estrutura de dados com `shape`, `pattern`, `primaryColor`, `secondaryColor`, `accentColor` e `symbol`.
- **Valores Padrão Automáticos:** Para clubes sem configuração: `shape = "shield"`, `pattern = "solid"`, `primaryColor = "#808080"`, `secondaryColor = "#FFFFFF"`, `accentColor = "#000000"`, `symbol = "initial"`.
- **Coluna no Banco (`badgeConfigJson`):** Adicionada a coluna `badgeConfigJson TEXT` na tabela `clubs`.
- **Migração de Banco (`MIGRATION_31_32`):** Versão do banco incrementada para `32` em `DatabaseContract.VERSION` com comando `ALTER TABLE clubs ADD COLUMN badgeConfigJson TEXT DEFAULT NULL`.

---

### 2. Componentes Vetoriais (`Canvas`)
- **`BadgeShapeDrawer.kt`:** Desenha as 5 formas (`shield`, `circle`, `diamond`, `hexagon`, `crest`).
- **`BadgePatternDrawer.kt`:** Aplica os 7 padrões visuais com clipping (`solid`, `vertical_stripes`, `horizontal_stripes`, `diagonal_stripes`, `half`, `cross`, `central_circle`).
- **`BadgeSymbolDrawer.kt`:** Desenha os 7 símbolos centrais (`initial` [1ª letra], `star`, `crown`, `lightning`, `ball`, `tree`, `none`).
- **`ClubBadge.kt`:** Composable unificado com preenchimento, padrão, símbolo central, borda e texto inicial.

---

### 3. Editor Visual de Escudo (`ClubsScreen.kt`)
- Subpreenchimento do diálogo `ClubEditorDialog` acessado ao clicar no lápis do clube.
- Controles interativos para forma, padrão, paleta compacta de cores e símbolo central.
- **Preview ao vivo em tempo real** mostrando o escudo renderizado instantaneamente conforme as opções são selecionadas.

---

### 4. Tamanhos Padronizados nas Telas
- **Lista de clubes (`ClubsScreen`):** `40.dp`
- **Perfil do clube (`ClubProfileScreen`):** `80.dp`
- **Jogos / Rodada (`ScheduleScreen` / `Dashboard`):** `32.dp`
- **Tabela de Classificação (`StandingsScreen`):** `24.dp`

---

### 5. Testes Automatizados (`ClubBadgeConfigTest.kt`)
- Valida os valores padrão de fallback.
- Valida a extração da 1ª letra em maiúsculo (ex: `"Athletico Paranaense"` $\rightarrow$ `"A"` e `"Manchester United"` $\rightarrow$ `"M"`).
- Valida a serialização e desserialização de/para JSON (`toJson` / `fromJson`).

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).
