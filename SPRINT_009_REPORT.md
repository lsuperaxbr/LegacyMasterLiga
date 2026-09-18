# Sprint 009 — Geração automática de rodadas e partidas

## Implementado

- Entidades Room `RoundEntity` e `MatchEntity`.
- DAOs reativos para rodadas, partidas e calendário detalhado.
- Migração Room 3 → 4, preservando os dados existentes.
- Gerador determinístico de confrontos:
  - somente ida: todos contra todos uma vez;
  - ida e volta: segundo turno com mandos invertidos;
  - jogo único: primeira fase eliminatória conforme a ordem das inscrições.
- Suporte a número ímpar de participantes, com folga automática no formato de pontos corridos e bye no jogo único.
- Chave única `(seasonId, pairingKey, leg)` para bloquear confrontos duplicados.
- Geração atômica dentro de uma transação Room.
- Bloqueio de nova geração quando a temporada já possui calendário.
- Tela Compose para selecionar liga/temporada, gerar e visualizar rodadas.
- Integração com Hilt, Navigation Compose e Dashboard.

## Regras

- São exigidos pelo menos dois clubes ativos inscritos.
- O Banco da Liga não participa, pois já é excluído no módulo de inscrições.
- Cada partida pertence obrigatoriamente a uma temporada e a uma rodada.
- A geração de uma temporada nunca altera placares ou calendário de outra.
