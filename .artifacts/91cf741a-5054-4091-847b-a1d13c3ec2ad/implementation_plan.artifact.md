# Plano de Implementação - Correção de Corrida de Threads no logBuffer

Este plano visa corrigir uma `IndexOutOfBoundsException` causada por acessos concorrentes não atômicos ao `logBuffer` na classe `OnlineSportsSyncManager`. Embora o buffer use `Collections.synchronizedList`, as operações de adição e remoção para manter o tamanho do buffer não são atômicas entre si.

## Revisão do Usuário Necessária

> [!IMPORTANT]
> A correção é cirúrgica e afeta apenas a proteção de acesso ao `logBuffer`. Nenhuma lógica de sincronização de dados ou negócios será alterada.

## Mudanças Propostas

### Feature: Online Sync

#### [MODIFICAR] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)

- **`syncLog`**: Envolver as operações `add` e `removeAt` em um bloco `synchronized(logBuffer)`. Substituir o `if` por um `while` para garantir que o tamanho seja mantido mesmo em cenários extremos, embora o `synchronized` já deva prevenir a intercalação.
- **`getDiagnosticInfo`**: Envolver o acesso `logBuffer.toList()` em um bloco `synchronized(logBuffer)` para garantir uma leitura consistente enquanto o buffer está sendo modificado por outras threads.

```kotlin
// Exemplo da mudança em syncLog
private fun syncLog(msg: String, error: Throwable? = null) {
    // ... setup ...
    synchronized(logBuffer) {
        logBuffer.add(0, "[$timestamp] $msg$suffix")
        while (logBuffer.size > 20) {
            logBuffer.removeAt(logBuffer.size - 1)
        }
    }
}
```

## Plano de Verificação

### Testes Automatizados
- **Build:** Executar `Clean Project` seguido de `Rebuild Project` para garantir que não houve erros de sintaxe.
- **Unit Tests:** Embora o problema seja de concorrência e difícil de reproduzir em testes unitários simples, rodar os testes existentes de `feature/online` para garantir regressão zero.

### Verificação Manual
- Validar a compilação do projeto.
