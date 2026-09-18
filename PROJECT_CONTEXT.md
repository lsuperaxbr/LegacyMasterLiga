# Project Context: Legacy Master Liga

## 1. Identidade e Propósito
O **Legacy Master Liga** é um gerenciador esportivo reativo para Android, focado em ligas personalizadas de futebol. O sistema permite a gestão completa de ligas, clubes, competições, finanças, mercado de transferências e auditoria de ações, garantindo persistência local e integridade de dados.

## 2. Stack Tecnológica (Definitiva)
- **Linguagem:** Kotlin (Java 17).
- **UI:** Jetpack Compose com Material 3.
- **Arquitetura:** MVVM (Model-View-ViewModel) + Clean Architecture + Repository Pattern.
- **Persistência:** Room Database (SQLite) — Versão Atual: **12**.
- **Injeção de Dependência:** Hilt (Dagger).
- **Navegação:** Navigation Compose com animações personalizadas.
- **Async/Stream:** Coroutines e Flow/StateFlow.
- **Build System:** Gradle Kotlin DSL com Version Catalog.
- **SDK:**
    - `compileSdk`: **36** (Android 16 - Oficial).
    - `minSdk`: **24**.
    - `targetSdk`: **36**.

## 3. Regras Arquiteturais
O projeto utiliza uma estrutura **Feature-First** combinada com **Clean Architecture**:
- **`core/`:** Componentes transversais e compartilhados (database, di, model, navigation, security, ui).
- **`feature/`:** Módulos funcionais encapsulados.
    - **Atenção:** Interfaces de repositório e modelos de domínio dentro de `feature/.../domain` são **intencionais** para manter o encapsulamento da funcionalidade e não devem ser movidas para o domínio global sem necessidade técnica justificada.
- **`domain/`:** Casos de uso globais e lógica de negócio que cruza múltiplas funcionalidades (ex: Inicialização de dados).
- **`data/`:** Implementações de repositórios globais e mapeadores de dados.

## 4. Definições de Domínio e Regras de Negócio
- **Moeda Oficial:** `CR` é a única moeda do sistema.
- **Banco da Liga:** Todo sistema financeiro orbita em torno do "Banco da Liga", um clube invisível e obrigatório que atua como a reserva central de `CR` para premiações e ajustes.
- **Perfis de Usuário:**
    - `ADMINISTRATOR`: Acesso total a configurações, auditoria, backups e gestão de usuários.
    - `PRESIDENT`: Acesso restrito ao Dashboard, Mercado e Financeiro do clube associado.
    - `VISITOR`: Acesso somente leitura; bloqueado em módulos administrativos e financeiros.
- **Auditoria:** Toda ação crítica (financeiro, resultados, logins) é registrada obrigatoriamente via `AuditLogger` na tabela `audit_logs`.
- **Notícias:** Motor determinístico interno que gera manchetes e textos baseados em eventos esportivos (transferências, goleadas, títulos), sem uso de IA externa.
- **Segurança:** Autenticação local com senhas protegidas por **PBKDF2** com salt aleatório.

## 5. Padrões de Persistência e Backup
- **Banco de Dados:** Nome oficial `legacy_master_liga.db`. Exportação de schema habilitada.
- **Backup:** Formato proprietário `.lmlbackup`. Consiste em um ZIP contendo o snapshot do banco SQLite e um manifesto JSON (`manifest.json`) com SHA-256 para verificação de integridade.
- **Restauração:** Processo protegido que exige a criação de um "Safety Backup" antes de sobrescrever os dados atuais.

## 6. Estado Atual do Projeto
- **Fase:** Beta Stabilization / RC (Release Candidate).
- **Build Variants:**
    - `debug`: Ambiente de desenvolvimento.
    - `beta`: Versão de teste com dados de demonstração (Liga M L Amigos).
    - `rc`: Release Candidate minificada com R8 habilitado.
    - `release`: Versão final otimizada para produção.
- **Diferencial:** O projeto já atingiu o estado de `BUILD SUCCESSFUL` com a configuração atual de dependências e SDK.

## 7. Regras Oficiais da Liga
- **Capacidade:** Cada liga suporta entre 3 e 20 clubes inscritos.
- **Coexistência:** Ligas e Copas podem existir simultaneamente no mesmo ecossistema.
- **Hierarquia:** Uma temporada pertence obrigatoriamente a uma competição.
- **Isolamento:** As classificações e estatísticas nunca são compartilhadas entre temporadas (snapshots imutáveis).
- **Multi-participação:** Os mesmos clubes podem disputar várias competições.
- **Sistemas de Disputa:**
    - **Liga:** Utiliza o sistema de pontos corridos.
    - **Copa:** Utiliza chaveamento independente (Eliminatórias).
- **Formatos de Confronto:**
    - Ida (Turno Único).
    - Ida e Volta (Turno e Returno).
    - Jogo Único (Exclusivo para fases de Copa).

## 8. Regras Financeiras
- **Sem Custos Recorrentes:** Não existem salários, renovações de contrato ou multas.
- **Operações Proibidas:** Não existem empréstimos de jogadores ou trocas entre clubes que não envolvam pagamento em `CR`.
- **Rastreabilidade:** Toda e qualquer movimentação de `CR` (ajustes, transferências, premiações) gera obrigatoriamente um registro no extrato (`financial_transactions`).
- **Imutabilidade:** É estritamente proibido apagar movimentações financeiras do banco de dados.
- **Correção de Erros:** Qualquer erro financeiro deve ser corrigido exclusivamente através de novos lançamentos de ajuste, mantendo o histórico original intacto.

## 9. Funcionalidades Removidas Definitivamente
**Nunca implementar:**
- IA para geração de notícias (manter motor determinístico);
- Gestão de estádios;
- Gestão de uniformes;
- Foto do presidente;
- Sistema de olheiros;
- Evolução/Progressão de atributos de jogadores;
- Cadastro completo de elenco (manter mercado simplificado por nome de jogador).

## 10. Escudos (Crests)
Os escudos são considerados recursos internos do aplicativo e sua gestão segue regras rigorosas de persistência:
- **Propriedade:** Uma vez importado, o escudo pertence ao aplicativo.
- **Armazenamento:** Todo escudo importado deve ser copiado para o armazenamento interno (`filesDir/crests`) com extensão detectada automaticamente.
- **Independência:** O aplicativo nunca deve depender da imagem original no dispositivo do usuário após a importação.
- **Ciclo de Vida:** Arquivos órfãos (não vinculados a nenhum clube) devem ser removidos automaticamente para otimizar o espaço.
- **Portabilidade:** Os escudos internos devem ser incluídos em todos os backups (`.lmlbackup`) e restaurados automaticamente para manter a integridade visual da liga.

## 11. Regras para Assistentes de IA
Para garantir a integridade e continuidade do projeto, todo assistente de IA deve seguir estas diretrizes:

### Antes de qualquer alteração:
- Ler integralmente o `PROJECT_CONTEXT.md`;
- Ler o `CHANGELOG.md`;
- Ler o `TECHNICAL_REPORT.md`.

### Proibições (Nunca):
- Atualizar dependências sem a existência de um erro real e documentado;
- Alterar a arquitetura base do projeto;
- Refatorar módulos inteiros;
- Mover arquivos por simples preferência pessoal ou de estilo;
- Alterar regras de negócio estabelecidas.

### Obrigações (Sempre):
- Corrigir apenas um problema por vez para facilitar a revisão;
- Mostrar o diff das alterações antes de aplicá-las;
- Listar todos os arquivos que serão alterados;
- Preservar o estado de `BUILD SUCCESSFUL` do projeto.

## 12. Estado Atual
- **Build:** `BUILD SUCCESSFUL` confirmado.
- **Versão:** `1.0.0-rc01` (Release Candidate).
- **Room Database:** Versão `12`.
- **Compile SDK:** `36`.
- **Arquitetura:** Estável (Feature-First + MVVM + Clean Arch).
- **Fase:** Release Candidate final.
