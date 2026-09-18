# Sprint 032 — Revisão funcional e dados de demonstração

## Objetivo
Preparar a Beta para o primeiro teste real sem alterar as regras oficiais da Liga M L Amigos.

## Implementado
- Dados de demonstração idempotentes apenas nas variantes Debug/Beta.
- Liga M L Amigos preservada com moeda CR.
- Clubes iniciais:
  - Torino — Tomascote — 500 CR
  - Fluminense — Richemont — 500 CR
  - Bournemouth — Pipocacr7 — 500 CR
  - Girona — LSuperax — 500 CR
- Usuários Presidente para os quatro clubes, todos com senha temporária `demo123`.
- Competição `Liga Principal`, em ida e volta, com `Temporada 1` ativa.
- Quatro clubes inscritos na temporada, sem duplicidade.
- Tela interna `Roteiro de teste Beta`, acessível pelo Dashboard administrativo.
- Roteiro com validação de login, clubes, participantes, calendário, resultados, classificação, mercado, financeiro, usuários e backup.

## Proteção da versão oficial
Os dados fictícios são criados somente quando `BuildConfig.DEBUG` é verdadeiro. A variante Release não recebe dados de demonstração.

## Banco de dados
- Room permanece na versão 12.
- Nenhuma migração foi necessária.
- A carga é idempotente e reutiliza registros já existentes.

## Versão
- `versionCode 33`
- `versionName 1.0.0-beta03`
