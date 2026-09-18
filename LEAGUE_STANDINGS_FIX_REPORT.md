# Relatório de Correção: Classificação da Liga (RC1.1)

Este documento detalha a resolução do problema onde a tabela de classificação das Ligas permanecia vazia após o lançamento de resultados ou inscrição de clubes.

## Causa Raiz Confirmada

O problema ocorria devido a dois fatores técnicos:
1. **Dependência de Partidas**: O motor de reconstrução da tabela (`rebuildStandings`) utilizava a tabela de `matches` para descobrir quais clubes deveriam aparecer na classificação. Se não houvesse partidas geradas ou finalizadas, a lista resultava vazia.
2. **Side-effects no Flow**: O `StandingsViewModel` tentava atualizar estados de seleção (`MutableStateFlow`) dentro de um bloco `combine`, o que causava emissões instáveis e falhas na atualização da UI quando os dados subjacentes mudavam.

## Arquivos Modificados

- `ResultsRepository.kt`: Adicionado o método `rebuildStandings` à interface pública.
- `RoomResultsRepository.kt`: Refatorada a lógica de reconstrução para utilizar os **participantes inscritos** como fonte primária de verdade, garantindo que a tabela exista com 0 jogos logo após a inscrição.
- `RoomCompetitionRepository.kt`: Adicionado gatilho para atualizar a tabela após a inscrição inicial de clubes ou criação de nova temporada.
- `RoomScheduleRepository.kt`: Adicionado gatilho para atualizar a tabela após a geração automática de partidas.
- `StandingsViewModel.kt`: Refatorada a composição de fluxos para eliminar efeitos colaterais e garantir inicialização determinística da seleção de Liga e Temporada.

## Resumo das Alterações

1. **Iniciação Antecipada**: Agora, assim que os clubes são inscritos em uma temporada, a tabela de classificação é gerada com todos os valores zerados.
2. **Integridade de Ligas**: Forçado o tratamento de `groupIndex` como nulo para competições do tipo Liga, evitando que filtros de grupos ocultem os dados.
3. **Fluxos Reativos**: A ViewModel agora observa as mudanças e atualiza a seleção apenas via `onEach`, mantendo o `combine` focado apenas na produção do estado visual.

## Testes Realizados

- **Cenário 1**: Criação de nova Liga e inscrição de clubes.
  - *Resultado*: Tabela apareceu imediatamente com 0 PTS e 0 J. **[OK]**
- **Cenário 2**: Lançamento de primeiro resultado (ex: 1x0).
  - *Resultado*: Vencedor atualizado para 3 PTS e 1 J instantaneamente. **[OK]**
- **Cenário 3**: Reabertura do aplicativo.
  - *Resultado*: Seleção de Liga e classificação mantidas integralmente. **[OK]**

## Build e APK

- **Build**: Finalizado com SUCESSO.
- **Testes Unitários**: 17 aprovados / 0 falhas.
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`

---
*Assinado eletronicamente pelo Assistente de IA.*
