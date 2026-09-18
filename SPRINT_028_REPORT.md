# Sprint 028 — Testes automatizados e controle de qualidade

## Objetivo
Adicionar uma base real de testes automatizados para regras de negócio, autenticação, permissões, geração de calendário, notícias, cache, banco Room e navegação.

## Testes locais (`app/src/test`)
- `AccessPolicyTest`: matriz de permissões de Administrador, Presidente e Visitante.
- `LoginUseCaseTest`: validação de campos e encaminhamento ao repositório.
- `ScheduleGeneratorTest`: turno único, ida e volta, número ímpar de clubes, duplicidades e entradas inválidas.
- `NewsTemplateFactoryTest`: goleadas, empates, transferências em CR e determinismo por evento.
- `AppMemoryCacheTest`: leitura, invalidação e `getOrPut`.

## Testes instrumentados (`app/src/androidTest`)
- `AppDatabaseIntegrationTest`: banco Room em memória, usuário, sessão, liga, clube, saldo em CR e integridade de chaves estrangeiras.
- `NavigationSmokeTest`: unicidade das rotas e criação da rota do perfil do clube.
- `PasswordHasherTest`: hash PBKDF2, salts independentes e rejeição de senha incorreta.
- O teste Room também cobre transferências, Banco da Liga e lançamentos financeiros relacionados.
- `BackupPackageTest`: manifesto, metadados e checksum SHA-256 dos arquivos de backup.

## Correções e proteções adicionadas
- A suíte verifica que visitantes não acessam Financeiro ou Mercado.
- A geração de calendário não cria confrontos do clube contra ele mesmo.
- O banco rejeita clubes órfãos sem liga válida.
- O saldo financeiro é validado pela soma imutável do extrato.
- Os modelos de notícias mantêm resultado determinístico para o mesmo evento.

## Execução recomendada
No Android Studio ou terminal com Gradle disponível:

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew lintDebug
```

Os testes instrumentados exigem um emulador ou aparelho Android conectado.
