# Sprint 020 — Encerramento oficial

Implementado encerramento manual de temporadas e competições, snapshot da classificação final, campeão, vice, premiações em CR, notícia automática, auditoria e bloqueio de resultados após fechamento.

## Banco
Room 10, com `season_closures` e `final_standings`.

## Segurança
Somente Administradores acessam o fluxo de encerramento. Todas as partidas devem estar finalizadas. Uma temporada só pode ser encerrada uma vez.
