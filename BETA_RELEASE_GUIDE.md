# Guia de Release Beta Oficial — Legacy Master Liga

Este documento oficializa a entrada do projeto na **Fase Beta Oficial**. A partir deste momento, a arquitetura e as regras de negócio estão **congeladas** para garantir a estabilidade necessária para o lançamento da versão 1.0.

---

## 1. Estado Atual do Projeto
- **Versão Atual:** `1.0.0-rc01` (Release Candidate).
- **Status do Build:** `BUILD SUCCESSFUL` confirmado em ambientes de homologação.
- **Persistência:** Room Database Versão **12**.
- **SDK:** `compileSdk 36`, `minSdk 24`.
- **Arquitetura:** Estável (Feature-First + MVVM + Clean Arch + Repository Pattern).

## 2. Funcionalidades Implementadas
O sistema conta com os seguintes módulos operacionais:
- **Acesso:** Autenticação via PBKDF2 e gestão de sessões persistentes.
- **Gestão Esportiva:** Ligas, Clubes (com escudos internos), Competições e Temporadas.
- **Operações:** Geração automática de Rodadas, lançamento de Partidas e Classificação reativa.
- **Economia:** Mercado de transferências e Financeiro (CR) com conservação de riqueza.
- **Inteligência:** Motor de notícias determinístico e Dashboard de comando.
- **Soberania de Dados:** Sistema de Backup/Restauração `.lmlbackup` com integridade SHA-256.
- **Transparência:** Auditoria completa de todas as ações administrativas.
- **Ferramentas:** Central de Relatórios (PDF/CSV), Hall da Fama e Estatísticas.

## 3. Funcionalidades Congeladas
**Proibido adicionar, remover ou refatorar:**
- Estrutura de pacotes e camadas arquiteturais.
- Dependências externas e versões de bibliotecas.
- Esquema de tabelas do banco de dados (exceto para correções críticas).
- Regras de conservação de `CR` e isolamento de temporadas.
- Fluxos de permissões (RBAC).

## 4. Procedimento para Registrar Bugs
Todo erro identificado deve ser registrado seguindo o padrão:
1. **ID do Caso de Teste:** Referência ao [TEST_CASES.md](TEST_CASES.md).
2. **Cenário:** Descrição breve do erro.
3. **Passos para Reproduzir:** Sequência exata de ações.
4. **Resultado Atual:** O que aconteceu de errado.
5. **Resultado Esperado:** O comportamento correto conforme a especificação.
6. **Logs:** Captura de Logcat (se aplicável).

## 5. Fluxo de Correção
1. **Auditoria:** O erro é validado contra o [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md).
2. **Análise de Impacto:** Verificação se a correção afeta outras regras congeladas.
3. **Correção Mínima:** Implementação do menor diff possível para sanar o problema.
4. **Validação:** Execução obrigatória dos testes de regressão financeira e backup.
5. **Registro:** Log de auditoria interna e atualização do [CHANGELOG.md](CHANGELOG.md).

## 6. Critérios para Aprovação da Versão 1.0
- **Bugs Críticos:** 0 (Zero).
- **Bugs de Alta Prioridade:** 0 (Zero).
- **Suíte de Testes:** 100% de aproveitamento nos 35 casos do [TEST_CASES.md](TEST_CASES.md).
- **Segurança:** Validação final do sistema de Backup com arquivos reais.
- **Estabilidade:** Ausência de Crashes ou ANRs em fluxo de uso intensivo.

## 7. Critérios para Abertura da Versão 2.0
- Lançamento oficial da versão 1.0 estável.
- Necessidade técnica de modularização real em módulos Gradle.
- Implementação de recursos do roadmap futuro (Sincronização Cloud, Cache avançado).

## 8. Checklist Final da Versão 1.0
- [ ] Executar `lintRc` e garantir conformidade total.
- [ ] Validar integridade do manifesto JSON em backups antigos.
- [ ] Verificar consistência visual em modo claro e escuro.
- [ ] Confirmar que o Banco da Liga reflete a soma exata de todos os CRs emitidos.
- [ ] Gerar AAB (Android App Bundle) assinado para produção.

---
> [!IMPORTANT]
> **O foco absoluto nesta fase é a ESTABILIDADE e CONFIABILIDADE. Inovações estão suspensas até a abertura da versão 2.0.**
