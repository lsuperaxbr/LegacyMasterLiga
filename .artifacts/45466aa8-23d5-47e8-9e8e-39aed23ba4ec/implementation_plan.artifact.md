# Plano de Implementação - Tela de Importação em Lote de Atributos por Time

Implementação da tela `ImportTeamAttributesScreen`, do parser `ImportTeamAttributesParser` e do caso de uso `ImportTeamAttributesUseCase` para atualizar em lote os atributos de todos os jogadores de um clube colando o texto de um arquivo `.txt` estruturado em chave-valor.

---

## Estrutura da Funcionalidade e Regras de Negócio

### 1. Parser do Texto (`ImportTeamAttributesParser.kt`)
- **Divisão em Blocos:** O texto colado pelo usuário é dividido em blocos de jogadores separados por linhas em branco (`\n\n` ou `\r\n\r\n`).
- **Extração Chave-Valor:** Para cada bloco, lê linhas no formato `Key: Value`.
- **Normalização de Chaves:** Ignora acentos e caracteres `_` de forma case-insensitive nas chaves, mantendo o valor original.
- **Mapeamento dos 26 Atributos PES 6:** Mapeia chaves reconhecidas para os 26 índices oficiais (`Attack`, `Defence`, `Balance`, `Stamina`, `Speed`, `Acceleration`, `Response`, `Agility`, `Dribble_Accuracy`, `Dribble_Speed`, `Short_Pass_Accuracy`, `Short_Pass_Speed`, `Long_Pass_Accuracy`, `Long_Pass_Speed`, `Shot_Accuracy`, `Shot_Power`, `Shot_Technique`, `Free_Kick_Accuracy`, `Swerve`, `Heading`, `Jump`, `Team_Work`, `Technique`, `Aggression`, `Mentality`, `GK_Skills`).
- **Regras de `Foot` e `Height`:**
  - Se `Foot:` ou `Height:` estiverem sem valor após os dois-pontos, registra como valor vazio/nulo.
  - Se possuírem valor (ex: `Height: 179`, `Foot: R`), salva o valor formatado.
- **Campos Desconhecidos:** Linhas com chaves não reconhecidas são ignoradas sem disparar exceção.

---

### 2. Caso de Uso (`ImportTeamAttributesUseCase.kt`)
- **Entrada:** `clubId: Long`, `rawText: String`.
- **Busca por Nome:** Para cada jogador parsed, realiza busca no banco pelo nome dentro do clube (`playerDao.findByNameAndClub(clubId, name)`).
- **Atualização Atômica:**
  - Se o jogador for encontrado: atualiza `position`, `overall`, `heightCm`, `preferredFoot` e reconstrói a string de 26 atributos numéricos (`attributesRaw`), gravando a entidade via `playerDao.update(player)`.
  - Se o jogador não for encontrado: registra o nome na lista de não encontrados (`unmappedPlayerNames`).
- **Resultado Retornado (`ImportTeamAttributesResult`):** Quantidade de jogadores atualizados e lista de nomes não mapeados.

---

### 3. Interface e Navegação (`ImportTeamAttributesScreen.kt` & ViewModel)
- **Rota:** `import_team_attributes?clubId={clubId}` em `LegacyDestination.kt` e `LegacyNavGraph.kt`.
- **Acesso:** Botão na aba **Elenco** do perfil do clube (`ClubProfileScreen.kt`) e card em **Configurações Avançadas** (`SettingsScreen.kt`).
- **Componentes da UI:**
  - Seletor Dropdown de Clubes (preenchendo o clube atual se aberto via perfil).
  - Campo multilinha grande (`OutlinedTextField`) para colar o conteúdo do `.txt`.
  - Botão de ação `"Atualizar Atributos do Time"`.
  - Cartão de Resultado: Exibe a quantidade de atletas atualizados e os nomes de jogadores não encontrados para conferência.

---

## Proposta de Mudanças Arquivos por Arquivo

### Core & Navigation

#### [MODIFY] [LegacyDestination.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyDestination.kt)
- Adicionar o destino `ImportTeamAttributes` com o parâmetro opcional `clubId`.

#### [MODIFY] [LegacyNavGraph.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavGraph.kt)
- Adicionar o composable e rota para `ImportTeamAttributesRoute`.

### Domain / Feature

#### [NEW] [ImportTeamAttributesParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/ImportTeamAttributesParser.kt)
- Utilitário de parsing de blocos chave-valor em texto.

#### [NEW] [ImportTeamAttributesUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/ImportTeamAttributesUseCase.kt)
- Caso de uso que executa a atualização no banco de dados local (Room).

### UI & Presentation

#### [NEW] [ImportTeamAttributesViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/presentation/ImportTeamAttributesViewModel.kt)
- ViewModel responsável pelo estado da tela de importação em lote de atributos.

#### [NEW] [ImportTeamAttributesScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/presentation/ImportTeamAttributesScreen.kt)
- Tela Jetpack Compose para seleção do clube, colagem de texto e exibição do resumo de atualização.

#### [MODIFY] [ClubProfileScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileScreen.kt)
- Adicionar o botão `"Importar Atributos (TXT)"` na aba de Elenco do clube.

### Unit Tests

#### [NEW] [ImportTeamAttributesUseCaseTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/feature/attributes/domain/ImportTeamAttributesUseCaseTest.kt)
- Testes unitários cobrindo:
  - Arquivo válido com 2 jogadores.
  - Caso com jogador não encontrado.
  - Caso com campos `Foot:` e `Height:` vazios.
  - Caso com campo desconhecido sendo ignorado.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários do recurso via Gradle: `gradle_build("app:assembleDebug")` e rodar a suíte `ImportTeamAttributesUseCaseTest`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).
