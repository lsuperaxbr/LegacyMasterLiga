# Sprint 013 — Mercado e Financeiro integrados

## Resultado

A Sprint 013 entrega os módulos Mercado e Financeiro conectados ao banco Room e ao sistema de permissões. Toda movimentação usa a moeda oficial CR e aparece imediatamente nas telas e no Dashboard por meio de `Flow`/`StateFlow`.

## Financeiro

- Saldo calculado pelo somatório do livro-caixa, sem coluna de saldo sujeita a dessincronização.
- Visão consolidada dos clubes e do Banco da Liga.
- Extrato completo, ordenado do lançamento mais recente para o mais antigo.
- Filtro de extrato por clube para administradores.
- Créditos e débitos realizados pelo Banco da Liga com dois lançamentos espelhados.
- Débito bloqueado quando o clube não possui saldo suficiente.
- Administrador movimenta o Banco; Presidente consulta somente o clube ao qual está associado.

## Mercado

- Negociação direta pelo nome do jogador, sem exigir cadastro de elenco.
- Seleção de clube vendedor, comprador e valor em CR.
- Validação de clubes ativos, mesma liga, origem diferente do destino e valor positivo.
- Validação do saldo do comprador antes de qualquer gravação.
- Presidente só pode iniciar compra para o próprio clube; Administrador negocia entre quaisquer clubes ativos da liga.
- Histórico completo de transferências.

## Consistência transacional

Cada transferência é executada dentro de uma única transação Room: validação do saldo, criação da transferência, débito do comprador e crédito do vendedor. Se qualquer etapa falhar, nenhuma alteração parcial é persistida. Ajustes do Banco da Liga seguem a mesma garantia atômica.

## Banco de dados

- Room incrementado da versão 5 para 6.
- Migração `5 → 6` adiciona aos lançamentos: tipo, clube contraparte e vínculo opcional com transferência.
- Dados existentes são preservados e recebem o tipo compatível `ADJUSTMENT`.
- DAOs ampliados com inserções, saldos, extratos e históricos reativos.

## Arquitetura e integração

- Novos pacotes `feature/finance` e `feature/market`, mantendo a separação `data/domain/presentation`.
- `FinanceRepository` e `RoomFinanceRepository` ligados pelo módulo Hilt.
- `FinanceViewModel` e `MarketViewModel` integrados à sessão e às permissões existentes.
- Novas telas Jetpack Compose ligadas às rotas reais do Navigation Compose.
- Dashboard atualizado automaticamente pelos lançamentos do livro-caixa.

## Versão

- `versionName`: `0.13.0-alpha13`
- `versionCode`: `13`
- Room: `6`

## Validação

- Schema Room 6 exportado em `app/schemas`, incluindo os novos metadados financeiros.
- Compilação Kotlin/Compose/Hilt/KSP concluída com sucesso.
- Verificação de metadados AndroidX concluída após alinhamento das dependências ao SDK 36.1.
- Suíte `testDebugUnitTest` concluída com `BUILD SUCCESSFUL`.
