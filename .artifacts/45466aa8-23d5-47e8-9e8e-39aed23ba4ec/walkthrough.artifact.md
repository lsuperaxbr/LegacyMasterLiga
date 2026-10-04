# Walkthrough - Importação em Lote de Atributos por Time (ImportTeamAttributes)

Implementada a nova funcionalidade de **Importação em Lote de Atributos por Time**, permitindo atualizar de uma só vez a posição, OVR, altura, pé e os 26 atributos numéricos PES 6 de todos os jogadores de um clube colando o texto de um arquivo `.txt` estruturado em chave-valor.

---

## O Que Foi Implementado

### 1. Parser de Blocos Chave-Valor (`ImportTeamAttributesParser.kt`)
- Divisão inteligente por linhas em branco (`\n\n` ou `\r\n\r\n`).
- Suporte flexível e resiliente a chaves em `camelCase`, `underscore` e maiúsculas/minúsculas (`Dribble_Accuracy`, `dribbleAccuracy`, `Dribble Accuracy` $\rightarrow$ normalizados para `dribbleaccuracy`).
- Mapeamento direto para os 26 atributos numéricos do banco do aplicativo.
- Preservação do sinal de campo em branco para `Foot:` e `Height:`.
- Ignora campos desconhecidos sem falhar.

---

### 2. Caso de Uso de Atualização no Banco (`ImportTeamAttributesUseCase.kt`)
- Recebe o `clubId` e o texto do arquivo `.txt`.
- Busca cada jogador no banco pelo nome dentro daquele clube (`playerDao.findByNameAndClub(clubId, name)`).
- Atualiza atomicamente a posição, OVR, `heightCm`, `preferredFoot` e a string de 26 atributos numéricos (`attributesRaw`).
- Retorna relatório completo com total de atletas no arquivo, atletas atualizados com sucesso e lista de nomes não encontrados.

---

### 3. Interface e Navegação Jetpack Compose
- **Nova Tela:** `ImportTeamAttributesScreen.kt` e `ImportTeamAttributesViewModel.kt`.
  - Seletor Dropdown de clubes da liga ativa.
  - Campo multilinha expansível para colagem do texto `.txt`.
  - Botão de ação com indicador de carregamento.
  - Cartão de resultado com resumo de sucessos e badge de alerta para atletas não encontrados.
- **Integração de Rota:** Registrado em `LegacyDestination.ImportTeamAttributes` (`"import_team_attributes?clubId={clubId}"`) e `LegacyNavGraph.kt`.
- **Pontos de Acesso:** Botão com ícone de upload na aba **Elenco** do perfil do clube (`ClubProfileScreen.kt`).

---

## Testes Automatizados (`ImportTeamAttributesUseCaseTest.kt`)

Suíte de testes aprovada cobrindo os 4 cenários mandatórios:
1. **Atletas Válidos:** Atualização simultânea de 2 atletas (Bruno Fernandes e Marcus Rashford) confirmada.
2. **Atleta Não Encontrado:** Atleta inexistente no clube é adicionado à lista `unmappedPlayerNames`.
3. **`Foot:` e `Height:` Vazios:** Valida a preservação e gravação de string/inteiro nulo no banco sem corromper o registro.
4. **Chaves Desconhecidas Ignoradas:** Chaves personalizadas são ignoradas e os atributos conhecidos são atualizados normalmente.

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).
