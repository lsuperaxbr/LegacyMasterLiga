# Sprint 014 — Motor Automático de Notícias

## Resultado

A Sprint 014 entrega um motor local de notícias sem IA, orientado aos eventos efetivamente persistidos pelo aplicativo. O conteúdo é montado por um catálogo determinístico de templates e gravado na mesma transação Room do evento de origem.

## Eventos cobertos

- **Transferência concluída:** informa jogador, vendedor, comprador e valor em CR.
- **Resultado:** publica o placar e os clubes envolvidos.
- **Goleada:** resultados com diferença mínima de três gols recebem categoria e textos especiais, substituindo a notícia comum da partida.
- **Mudança de liderança:** compara o líder antes e depois da reconstrução oficial da classificação.
- **Conquista:** quando todas as partidas da temporada terminam, encerra a temporada e publica o campeão.

## Conteúdo sem IA

O `NewsTemplateFactory` possui múltiplas variações para cada categoria. A escolha usa a chave estável do evento com `Math.floorMod`, produzindo variedade entre eventos e exatamente o mesmo conteúdo em reprocessamentos. Nenhuma rede, modelo externo ou serviço de IA é utilizado.

## Deduplicação e correções

- Cada evento possui uma chave única: `TRANSFER:<id>`, `MATCH:<id>`, `LEADERSHIP:<matchId>` ou `CHAMPION:<seasonId>`.
- O índice único `index_news_dedupKey` garante a regra mesmo sob concorrência.
- Reprocessar uma transferência ou partida não aumenta a quantidade de notícias.
- Corrigir um placar substitui a matéria da partida pela versão atualizada.
- Antes de recalcular uma mudança de liderança, a consequência anterior da mesma partida é removida; ela só é recriada se a mudança continuar válida.
- A notícia de campeão é atualizada pela mesma chave caso a correção do último resultado altere o vencedor.

## Contexto correto

- Toda notícia possui `leagueId` obrigatório.
- Resultados, goleadas, liderança e conquistas recebem `competitionId` e `seasonId` derivados da própria partida, nunca da seleção da interface.
- Transferências pertencem à liga, mas não a uma competição ou temporada específica; por isso esses dois vínculos permanecem nulos, evitando associação artificial.
- A tela exibe o caminho `Liga • Competição • Temporada` sempre que o contexto esportivo existe.

## Room e migração

- Banco atualizado da versão 6 para 7.
- A tabela `news` foi reconstruída para adicionar FKs opcionais de competição e temporada.
- Foram adicionados `eventType`, `sourceId` e `dedupKey`.
- Notícias antigas são preservadas com chaves `LEGACY:<id>`.
- Índices adicionados para liga, competição, temporada, publicação e deduplicação.
- Schema 7 exportado em `app/schemas`.

## Arquitetura e integrações

- Eventos de domínio selados em `feature/news/domain`.
- `NewsEventPublisher` desacopla o motor dos repositórios de Mercado e Resultados.
- `RoomNewsEventPublisher` e `RoomNewsRepository` ligados por Hilt.
- `NewsViewModel`, `NewsRoute` e `NewsScreen` usam Flow, StateFlow e Compose.
- Navigation substituiu a tela provisória pela rota real de Notícias.
- Dashboard continua reativo ao total de notícias e agora identifica o conteúdo como automático.

## Validação

- Compilação Kotlin, Compose, Room/KSP e Hilt concluída sem erros.
- Schema Room 7 gerado e inspecionado.
- `testDebugUnitTest`: 5 testes, 0 falhas e 0 erros.
- Testes específicos confirmam determinismo, variedade e limiar de goleada.

## Versões

- Aplicativo: `0.14.0-alpha14`
- `versionCode`: `14`
- Room: `7`
