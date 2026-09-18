# Foundation: Legacy Master Liga

Este documento constitui a **Constituição Técnica** oficial do projeto **Legacy Master Liga**. Ele define os pilares arquiteturais, as regras de negócio definitivas e os padrões de desenvolvimento obrigatórios para garantir a integridade, escalabilidade e continuidade do sistema.

---

## 1. Visão Geral do Projeto
O **Legacy Master Liga** é um gerenciador esportivo reativo para Android, focado na organização de ligas personalizadas de futebol. O sistema prioriza a soberania dos dados locais, a precisão financeira e uma experiência de usuário imersiva, funcionando de forma totalmente offline e independente de serviços externos.

## 2. Objetivos
- Prover uma ferramenta completa para gestão de campeonatos (Ligas e Copas).
- Garantir 100% de integridade financeira através da moeda virtual `CR`.
- Oferecer um motor de notícias automático e determinístico para engajamento.
- Assegurar a portabilidade total dos dados via sistema proprietário de backup/restauração.
- Manter transparência administrativa através de logs de auditoria detalhados.

## 3. Arquitetura
O projeto segue uma arquitetura híbrida de alto desempenho:
- **Feature-First:** Organização por domínios funcionais (ex: `feature/finance`, `feature/market`).
- **MVVM (Model-View-ViewModel):** Separação estrita entre lógica de apresentação e UI.
- **Clean Architecture:** Camadas de domínio (`domain`) isoladas da persistência e da UI.
- **Repository Pattern:** Abstração total da fonte de dados, expondo fluxos reativos (`Flow`).
- **UDF (Unidirectional Data Flow):** Estado flui para baixo; eventos fluem para cima.

## 4. Stack Tecnológica Oficial
- **Linguagem:** Kotlin 2.2.10 (Java 17).
- **SDK:** `compileSdk 36` (Android 16), `minSdk 24`, `targetSdk 36`.
- **UI:** Jetpack Compose com Material 3.
- **Persistência:** Room Database v12 (SQLite).
- **Injeção de Dependência:** Hilt (Dagger).
- **Navegação:** Navigation Compose com transições customizadas (`LegacyMotion`).
- **Concorrência:** Coroutines & Flow/StateFlow.
- **Build:** Gradle Kotlin DSL com Version Catalog.

## 5. Estrutura Completa de Diretórios
```text
com.example.legacymasterliga/
├── core/                   # Componentes transversais e compartilhados
│   ├── database/           # AppDatabase, DAOs e Entidades globais
│   ├── di/                 # Módulos Hilt (Database, Repository)
│   ├── model/              # Enums (UserRole, SeasonStatus) e modelos base
│   ├── navigation/         # NavGraph, Destinos e ViewModels de rota
│   ├── security/           # Hashing (PBKDF2) e Políticas de Acesso
│   ├── session/            # Gestão de Sessão do Usuário
│   ├── storage/            # Gestão de arquivos físicos (CrestStorage)
│   └── ui/                 # Temas, Design Tokens e Componentes (Material 3)
├── data/                   # Implementações globais e mapeadores de domínio
├── domain/                 # Casos de uso e modelos de negócio globais
└── feature/                # Módulos funcionais encapsulados
    └── <nome_da_feature>/
        ├── data/           # Repositórios Room específicos
        ├── domain/         # Modelos e interfaces da funcionalidade
        └── presentation/   # Telas (Screen/Route) e ViewModels
```

## 6. Convenções de Nomenclatura
- **Telas:** `[Feature]Screen.kt` (UI) e `[Feature]Route.kt` (Ponto de entrada/ViewModel binding).
- **Lógica:** `[Feature]ViewModel.kt` e `[Feature]Repository.kt`.
- **Persistência:** `[Name]Entity.kt` (Tabelas) e `[Name]Dao.kt` (Consultas).
- **Implementação:** `Room[Name]Repository.kt`.
- **Mapeamento:** Extensões `toDomain()` e `toEntity()`.

## 7. Regras de Negócio Definitivas
- **Capacidade:** Ligas suportam de **3 a 20 clubes**.
- **Isolamento de Dados:** Classificações e estatísticas são exclusivas de cada temporada (snapshot imutável ao encerrar).
- **Banco da Liga:** Entidade obrigatória e invisível que centraliza a reserva de `CR`.
- **Escudos:** Pertencem ao app; copiados para armazenamento interno e incluídos no backup.
- **Resultados:** Podem ser corrigidos a qualquer momento (se a temporada estiver ativa), disparando recálculo atômico da classificação.

## 8. Fluxo de Autenticação
1. Entrada de Usuário/Senha.
2. Hash via **PBKDF2** (120k iterações) comparado em tempo constante.
3. Criação de sessão persistente no banco de dados.
4. Auditoria obrigatória do evento de Login/Logout.
5. Invalidação automática de sessão para contas bloqueadas ou desativadas.

## 9. Fluxo de Competições
- **Criação:** Uma competição deve definir seu formato (Liga/Copa) e regras de pontuação/desempate.
- **Geração de Rodadas:** Automatizada no início de cada temporada.
- **Encerramento:** Operação manual que valida todos os resultados, premia os vencedores e arquiva a tabela.

## 10. Fluxo Financeiro (CR)
- **Imutabilidade:** Lançamentos financeiros nunca são apagados ou alterados.
- **Ajustes:** Erros são corrigidos com novos lançamentos de ajuste (`BANK_ADJUSTMENT`).
- **Espelhamento:** Toda operação entre clubes gera dois lançamentos de sinais opostos para conservação do total de `CR`.
- **Atomicidade:** Transações financeiras utilizam `withTransaction` do Room.

## 11. Fluxo de Mercado
- **Transferências:** Realizadas apenas por nome do jogador (sem cadastro prévio de elenco).
- **Validação:** Saldo do comprador é verificado antes de iniciar a transação atômica.
- **Feedback:** Notícia automática gerada e publicada imediatamente após o sucesso da compra.

## 12. Fluxo de Notícias Automáticas
- **Motor:** `NewsTemplateFactory` utiliza eventos determinísticos.
- **Deduplicação:** Cada notícia possui uma `dedupKey` para evitar duplicidade em recálculos de placar.
- **Templates:** Seleção de manchetes baseada no hash do evento para variedade gramatical.

## 13. Fluxo de Auditoria
- Centralizado no `AuditLogger`.
- Registro obrigatório de quem (ator), o quê (ação), onde (entidade) e detalhes.
- Exibição em tela administrativa protegida por perfil.

## 14. Fluxo de Backup e Restauração
- **Formato:** ZIP proprietário `.lmlbackup`.
- **Conteúdo:** Banco SQLite, manifesto JSON com SHA-256 e pasta `crests/`.
- **Restauração:** Exige "Safety Backup" preventivo e reinicialização segura do banco de dados.

## 15. Fluxo de Permissões
- **ADMINISTRATOR:** Acesso total (Gestão, Auditoria, Backup).
- **PRESIDENT:** Gestão financeira e mercado restritos ao próprio clube.
- **VISITOR:** Acesso somente leitura; menus administrativos ocultos.
- **Proteção:** Rotas validadas no `LegacyNavGraph` via `AdminOnly`.

## 16. Padrões de Desenvolvimento Obrigatórios
- **Reatividade:** Uso de `collectAsStateWithLifecycle()` em todas as telas.
- **Otimização:** Uso de `distinctUntilChanged()` e `conflate()` em repositórios.
- **Segurança:** Nunca trafegar ou armazenar senhas em texto plano.
- **Estilo:** Seguir Material 3 Design System e tokens de design oficiais.

## 17. Checklist Pré-Commit
- [ ] O código compila sem erros (`BUILD SUCCESSFUL`).
- [ ] Nenhuma dependência foi atualizada sem necessidade.
- [ ] Regras de negócio de `CR` e `Banco da Liga` permanecem intactas.
- [ ] Auditoria incluída em novas operações críticas.
- [ ] Testes unitários/instrumentados passaram com sucesso.

## 18. Checklist Pré-Release
- [ ] Versão e `versionCode` atualizados no Gradle.
- [ ] Executado `lintRc` e corrigidos avisos críticos.
- [ ] Build de `release` minificado (R8) validado.
- [ ] Manifesto de backup testado em dispositivo físico.
- [ ] Chave de assinatura (`keystore`) protegida e verificada.

## 19. Funcionalidades Removidas Definitivamente
**Proibido implementar:**
- Inteligência Artificial para geração de textos/notícias.
- Gestão visual de estádios ou uniformes.
- Evolução de atributos/pontuação de jogadores individuais.
- Fotos de perfil de presidentes.
- Sistema de olheiros (scouting).

## 20. Roadmap
### Versão 1.0 (Estabilização)
- Finalização da Release Candidate (RC).
- Refinamento de performance de recomposição do Dashboard.
- Validação total do sistema de backup com múltiplas imagens.

### Versão 2.0 (Evolução Técnica)
- Modularização real de módulos Gradle (`:core`, `:feature`).
- Implementação de cache de imagens avançado (Coil).
- Adição de testes de regressão financeira (conservação de CR).
- Sincronização opcional entre dispositivos (Cloud Sync).
