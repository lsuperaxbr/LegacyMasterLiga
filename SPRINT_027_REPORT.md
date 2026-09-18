# Sprint 027 — Desempenho, Cache e Resiliência

## Objetivo

Reduzir trabalho desnecessário na interface e nas consultas reativas, adicionar cache local seguro para dados derivados, centralizar tratamento de falhas recuperáveis e disponibilizar monitoramento local para o Administrador.

## Implementações

### Monitoramento local
- Criado `PerformanceMonitor`, sem telemetria externa.
- Métricas agregadas por operação: quantidade, média, última execução e maior duração.
- Inicialização do aplicativo, composição do resumo do Dashboard e geração/cópia de relatórios passam a registrar duração.
- Criada tela administrativa **Desempenho** para consultar e limpar métricas.

### Cache local
- Criado `AppMemoryCache` com política LRU, limite de 64 entradas e expiração por TTL.
- Opções de Liga, Competição e Temporada dos relatórios são reutilizadas por cinco minutos.
- O cache pode ser inspecionado e limpo pelo Administrador.

### Tratamento de erros
- Criado `AppErrorReporter` para registrar localmente até 20 falhas recuperáveis.
- Falhas de inicialização, Dashboard e relatórios passam a ser registradas com mensagem segura.
- A tela **Desempenho** permite consultar e limpar o histórico local.

### Room e fluxos
- Consultas do Dashboard usam `distinctUntilChanged()` para evitar emissões equivalentes.
- O fluxo consolidado usa `conflate()` para descartar atualizações intermediárias obsoletas.
- Falhas no resumo não encerram a aplicação: o Dashboard apresenta estado seguro e registra o erro.
- Consultas do Dashboard continuam limitadas aos três resultados, transferências e notícias mais recentes.

### Compose
- Removido `LazyVerticalGrid` aninhado com altura calculada dentro do `LazyColumn` do Dashboard.
- Os atalhos agora são renderizados em linhas preguiçosas de dois cartões.
- A lista de módulos usa `remember` para evitar recriação em recomposições sem alteração relevante.
- Mantidas chaves estáveis para os itens.

## Banco de dados

Nenhuma tabela ou migração foi necessária. O Room permanece na versão 12.

## Versão

- `versionCode`: 28
- `versionName`: `0.27.0-alpha27`

## Validação

- Estrutura de arquivos e referências revisadas estaticamente.
- A tentativa de compilação não avançou porque o Gradle Wrapper precisa acessar `services.gradle.org`, indisponível neste ambiente.
