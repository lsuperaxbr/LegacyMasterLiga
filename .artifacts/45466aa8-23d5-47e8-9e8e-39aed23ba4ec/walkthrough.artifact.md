# Walkthrough - Exportação de Elencos PLVR (TXT por Jogador) & Remoção do Leitor CSV

Concluída a implementação do módulo de exportação de elencos em **arquivos `.txt` individuais por jogador** (padrão PLVR para PES 6) salvos na estrutura de pastas por clube em `Download/LegacyMasterLiga/`, além da **remoção integral e higienização do leitor legado de planilhas CSV**.

---

## O Que Foi Implementado

### 1. Novo Módulo de Exportação PLVR (`ExportPes6PlvrUseCase.kt`)
- **Arquivos `.txt` Individuais por Atleta:** Cada jogador tem seu próprio arquivo de texto nomeado com o nome do jogador sanitizado (sem acentos NFD):
  - `Download/LegacyMasterLiga/Manchester_United/Cristiano_Ronaldo.txt`
  - `Download/LegacyMasterLiga/Banco_da_Liga/Jogador_Livre.txt`
- **Proibidos Delimitadores `[PLAYER]`:** Os arquivos do jogador **não possuem cabeçalho/delimitador de bloco `[PLAYER]`**, contendo puramente as linhas de chave-valor em UTF-8 sem BOM.
- **Mapeamento Oficial dos 26 Atributos PES 6:** Todos os 26 atributos numéricos do banco de dados são exportados na ordem exata:
  `Attack`, `Defence`, `Balance`, `Stamina`, `Speed`, `Acceleration`, `Response`, `Agility`, `Dribble_Accuracy`, `Dribble_Speed`, `Short_Pass_Accuracy`, `Short_Pass_Speed`, `Long_Pass_Accuracy`, `Long_Pass_Speed`, `Shot_Accuracy`, `Shot_Power`, `Shot_Technique`, `Free_Kick_Accuracy`, `Swerve`, `Heading`, `Jump`, `Team_Work`, `Technique`, `Aggression`, `Mentality`, `GK_Skills`.
- **Preservação de `Height:` e `Foot:`:** Se o jogador tiver altura ou pé nulo/em branco no app, a linha correspondente é **mantida e gravada como `Height:` ou `Foot:`** (com o valor em branco após os dois-pontos, nunca omitindo a linha do campo).

---

### 2. Remoção Integral do Leitor Legado de CSV (Seção 12)
- **Descontinuação Completa:**
  - Deletados os arquivos `CsvRosterParser.kt`, `ImportCsvRostersUseCase.kt`, `CsvImportModels.kt`, `CsvImportDialog.kt` e `CsvRosterParserTest.kt`.
- **Higienização de Telas e ViewModel:**
  - Removido o card/botão de importação CSV e seletores de arquivos de `SettingsScreen.kt` e `SettingsViewModel.kt`.
  - Auditagem concluída para garantir ausência de dead clicks, botões sem ação ou menus órfãos.
- **Preservação do Banco de Dados:** Todos os dados locais de clubes e jogadores já cadastrados no Room/SQLite permanecem intactos.

---

### 3. Interface de Exportação (`SettingsScreen.kt`)
- Adicionado o botão `"Exportar Elencos (Pastas e TXT)"`.
- Diálogo de confirmação com opção de checkbox: `"Recriar pastas do zero (Limpeza Total)"` para apagar pastas órfãs de clubes ou jogadores excluídos antes de recriar a estrutura de diretórios.
- Diálogo de progresso e resumo final da operação.

---

## Testes Automatizados (`ExportPes6PlvrUseCaseTest.kt`)

- **Validação de Sanitização:** Confirma que `"São Paulo"` vira `"Sao_Paulo"` e `"Cristiano Ronaldo!"` vira `"Cristiano_Ronaldo"`.
- **Validação de Formato PLVR sem `[PLAYER]`:** Garante ausência da tag `[PLAYER]` e presença dos 26 atributos PES 6.
- **Validação de `Height:` e `Foot:` Nulos:** Confirma que ao passar valores nulos, as linhas `Height:` e `Foot:` permanecem presentes com valor em branco após os dois-pontos.

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).
