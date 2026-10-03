# Plano de Implementação - Exportação em Pastas com TXT Individual por Jogador & Remoção do Leitor CSV

Plano de execução atualizado com todas as correções obrigatórias da especificação técnica:
1. Geração de **arquivos `.txt` individuais por jogador** (sem delimitadores `[PLAYER]`).
2. Exportação exata dos **26 atributos de habilidade PES 6** do banco do aplicativo na ordem oficial.
3. Preservação das linhas `Height:` e `Foot:` mesmo se o valor for apaga/nulo (linha nunca omitida).
4. Remoção e limpeza integral do leitor legado de planilhas CSV.

---

## 1. Módulo de Exportação PLVR/TXT por Jogador (`Download/LegacyMasterLiga/`)

### Estrutura de Pastas e Arquivos Individuais
- **Diretório Raiz:** `Download/LegacyMasterLiga/` (`Environment.DIRECTORY_DOWNLOADS/LegacyMasterLiga`).
- **Nomenclatura Sanitizada NFD (Sem Acentos e Sem Caracteres Especiais):**
  - Pastas de Clubes: Espaços substituídos por sublinhados (`_`), acentos/caracteres especiais removidos NFD (exemplo: `"São Paulo"` $\rightarrow$ `"Sao_Paulo"`, `"Manchester United"` $\rightarrow$ `"Manchester_United"`).
  - Arquivos de Jogadores: Cada atleta possui seu **próprio arquivo `.txt`** nomeado com o nome do jogador sanitizado (exemplo: `"Cristiano_Ronaldo.txt"`, `"Bruno_Fernandes.txt"`).
  - Jogadores sem contrato/clube salvos individualmente dentro da pasta `"Banco_da_Liga/"`.

**Estrutura de Diretórios Exemplo:**
```text
Download/LegacyMasterLiga/
├── Manchester_United/
│   ├── Cristiano_Ronaldo.txt
│   ├── Bruno_Fernandes.txt
│   └── Marcus_Rashford.txt
├── Real_Madrid/
│   ├── Vinicius_Jr.txt
│   └── Jude_Bellingham.txt
└── Banco_da_Liga/
    └── Jogador_Livre.txt
```

---

### Conteúdo do Arquivo `.txt` do Jogador (UTF-8 sem BOM e Sem `[PLAYER]`)
- **Proibido:** Qualquer delimitador de bloco como `[PLAYER]`.
- **Formato:** Cada arquivo `.txt` contém exclusivamente as propriedades chave-valor do atleta em UTF-8 sem BOM.
- **Tratamento de `Height` e `Foot`:**
  - Padrão para novo jogador: `Height: 180`, `Foot: R`.
  - Se o usuário apagar o valor no app/banco (valor nulo/vazio), grava a linha preservada com o campo em branco (`Height:` ou `Foot:`), **nunca omitindo a linha do campo**.

**Exemplo do Arquivo `.txt` de um Atleta (`Cristiano_Ronaldo.txt`):**
```text
Name: Cristiano Ronaldo
Position: ATA
OVR: 91
Age: 37
Foot: R
Height: 187
Attack: 92
Defence: 42
Balance: 86
Stamina: 83
Speed: 87
Acceleration: 86
Response: 88
Agility: 94
Dribble_Accuracy: 88
Dribble_Speed: 86
Short_Pass_Accuracy: 81
Short_Pass_Speed: 80
Long_Pass_Accuracy: 78
Long_Pass_Speed: 76
Shot_Accuracy: 91
Shot_Power: 90
Shot_Technique: 88
Free_Kick_Accuracy: 85
Swerve: 82
Heading: 94
Jump: 95
Team_Work: 82
Technique: 89
Aggression: 91
Mentality: 85
GK_Skills: 50
```

---

### Mapeamento Oficial dos 26 Atributos PES 6 do Banco de Dados
A ordem e nomenclatura das chaves correspondem exatamente aos 26 atributos numéricos armazenados no app (`PlayerEntity.attributesRaw`):
1. `Attack` | 2. `Defence` | 3. `Balance` | 4. `Stamina` | 5. `Speed` | 6. `Acceleration`
7. `Response` | 8. `Agility` | 9. `Dribble_Accuracy` | 10. `Dribble_Speed`
11. `Short_Pass_Accuracy` | 12. `Short_Pass_Speed` | 13. `Long_Pass_Accuracy` | 14. `Long_Pass_Speed`
15. `Shot_Accuracy` | 16. `Shot_Power` | 17. `Shot_Technique` | 18. `Free_Kick_Accuracy`
19. `Swerve` | 20. `Heading` | 21. `Jump` | 22. `Team_Work` | 23. `Technique`
24. `Aggression` | 25. `Mentality` | 26. `GK_Skills`

---

### Interface e Limpeza Total de Pastas
- **Acesso:** Botão `"Exportar Elencos (Pastas e TXT)"` na tela de Configurações (`SettingsScreen.kt`).
- **Diálogo de Confirmação:**
  - Opção/Checkbox: `"Recriar pastas do zero (Limpeza Total)"` para apagar a pasta `Download/LegacyMasterLiga/` inteira antes da geração, eliminando clubes ou arquivos de atletas excluídos.
- **Feedback Visual:** Progresso assíncrono em `Dispatchers.IO` e mensagem ao concluir: `"Exportação concluída com sucesso! Arquivos salvos em Download/LegacyMasterLiga"`.

---

## 2. Remoção Integral do Leitor de CSV (Seção 12)

### Componentes a Serem Removidos / Limpos:
1. **Parsers e Usings:**
   - `CsvRosterParser.kt`
   - `ImportCsvRostersUseCase.kt`
   - `CsvImportModels.kt` (`CsvParseResult`, `CsvRawPlayer`, `CsvTeamMapping`, `CsvImportProgress`, `CsvImportSummary`)
   - `CsvRosterParserTest.kt`
2. **Interface e Diálogos:**
   - `CsvImportDialog.kt`
   - Botão e card de "Importação de Elencos (CSV)" em `SettingsScreen.kt`.
   - `csvLauncher`, chamadas de importação CSV e estados do CSV em `SettingsViewModel.kt`.
3. **Auditagem sem Menus Órfãos:**
   - Remoção completa sem deixar botões desabilitados, dead clicks ou rotas pendentes.

---

## Proposta de Mudanças Arquivos por Arquivo

### Exportador PLVR (Novo Componente)

#### [NEW] [ExportPes6PlvrUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/usecase/ExportPes6PlvrUseCase.kt)
- Lógica de varredura de clubes e jogadores, sanitização NFD de nomes de pastas e arquivos `.txt`, geração do arquivo individual por atleta em UTF-8 sem `[PLAYER]`, exportação dos 26 atributos PES 6 e salvamento em `Download/LegacyMasterLiga/`.

### Remoção de Arquivos Legados do CSV

#### [DELETE] [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
#### [DELETE] [ImportCsvRostersUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/usecase/ImportCsvRostersUseCase.kt)
#### [DELETE] [CsvImportModels.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/model/CsvImportModels.kt)
#### [DELETE] [CsvImportDialog.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/settings/presentation/CsvImportDialog.kt)
#### [DELETE] [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)

### UI & Presentation

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/settings/presentation/SettingsViewModel.kt)
- Injetar `ExportPes6PlvrUseCase`.
- Remover estados do CSV e expor a ação de exportação PLVR.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/settings/presentation/SettingsScreen.kt)
- Remover o card/botão de importação CSV.
- Adicionar o botão e diálogo do exportador PLVR.

### Unit Tests

#### [NEW] [ExportPes6PlvrUseCaseTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/usecase/ExportPes6PlvrUseCaseTest.kt)
- Testar a geração do conteúdo do `.txt` individual do jogador (sem `[PLAYER]`), os 26 atributos PES 6, preservação das linhas `Height:` e `Foot:` quando nulos/vazios e sanitização NFD dos nomes.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários do exportador via Gradle: `gradle_build("app:assembleDebug")` e rodar a suíte `ExportPes6PlvrUseCaseTest`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).
