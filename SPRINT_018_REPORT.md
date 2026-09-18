# Sprint 018 — Histórico de temporadas e competições

## Objetivo
Disponibilizar consulta histórica por liga, competição e temporada, preservando campeões, vice-campeões, classificação e estatísticas consolidadas.

## Implementado
- Novo módulo `history` em camadas `data`, `domain` e `presentation`.
- Consulta reativa de todas as temporadas de uma liga.
- Filtros por liga, competição e temporada.
- Campeão e vice derivados da classificação registrada da temporada.
- Classificação final completa com J, V, E, D, GP, GC, SG, pontos e aproveitamento.
- Resumo histórico com quantidade de temporadas, competições, partidas concluídas e gols.
- Acesso ao perfil do clube diretamente pela classificação histórica.
- Integração com Dashboard, Navigation Compose, Room e Hilt.

## Banco de dados
Nenhuma nova tabela foi necessária. O histórico usa as temporadas, partidas e classificações já preservadas pelo banco. A versão Room permanece 9.

## Versão
- versionCode: 18
- versionName: 0.18.0-alpha18
