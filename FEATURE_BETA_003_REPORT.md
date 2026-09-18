# Relatório de Implementação: Mercado Aprimorado — Issue FEATURE-BETA-003

Este documento detalha as melhorias no Mercado do Legacy Master Liga e a migração de banco de dados para suporte a trocas e novos tipos de operação.

## Arquivos Modificados

- `TransferEntity.kt`: Adicionados campos `type`, `swapId` e `note`.
- `AppDatabase.kt`: Versão elevada para 13.
- `DatabaseModule.kt`: Implementada `MIGRATION_12_13` para preservação integral de dados.
- `FinanceRepository.kt` & `RoomFinanceRepository.kt`: Implementada lógica de trocas atômicas, contratação livre, liberação e tratamento de 0 CR.
- `NewsEvents.kt` & `NewsTemplateFactory.kt`: Adicionados eventos de troca e 6 novos modelos de notícias determinísticas.
- `MarketViewModel.kt`: Adicionado suporte a `isLoading` e novo método `swap`.
- `MarketScreen.kt`: Diálogo de transferência dinâmico e suporte a múltiplos jogadores.

## Regras Implementadas

1. **Tipos de Operação**: Suporte explícito para Compra, Gratuita, Contratar Livre, Liberar e Troca.
2. **Contratação Livre (Banco -> Clube)**:
    - Com valor > 0: Fluxo financeiro normal.
    - Com valor = 0: Registro de mercado e notícia, sem lançamento financeiro.
3. **Liberação (Clube -> Banco)**: Retorno de jogador ao mercado sem movimentação financeira automática.
4. **Troca de Jogadores**:
    - Registro de dois movimentos simultâneos vinculados pelo mesmo `swapId`.
    - Suporte a compensação financeira opcional paga por um dos clubes.
5. **Conservação de CR**: Operações com valor 0 CR não geram lançamentos no extrato, mantendo a integridade matemática da liga.
6. **Prevenção de Cliques Duplos**: Implementado estado `isLoading` na ViewModel.

## Resultado da Migração (v12 -> v13)

- **Estratégia**: `MIGRATION_12_13` explícita adicionando colunas sem perda de dados.
- **Preservação**: Registros antigos preenchidos com `type = 'TRANSFER'`.
- **Integridade**: Validada via teste de fumaça e compilação.

## Resultados dos Testes

- **Testes Unitários**: 17 aprovados / 0 falhas.
- **Build**: `assembleDebug` finalizado com **BUILD SUCCESSFUL**.
- **APK**: APK de depuração v13 gerado com sucesso.

---
*Assinado eletronicamente pelo Assistente de IA.*
