# Sprint 003 — Domínio, Repositories e Inicialização

## Objetivo

Adicionar a primeira camada funcional entre o banco Room e as regras de negócio, além de preparar dados mínimos para a primeira execução do Legacy Master Liga.

## Arquitetura implementada

```text
domain/
├── model/
├── repository/
└── usecase/

data/
├── mapper/
└── repository/

core/
├── di/RepositoryModule.kt
└── security/PasswordHasher.kt
```

## Dados padrão

A inicialização é idempotente e executada dentro de uma transação Room. Caso os registros já existam, eles são reutilizados e não são duplicados.

- Primeiro Administrador: `admin`
- Senha temporária: `admin123`
- Primeira Liga: `Liga M L Amigos`
- Moeda: `CR`
- Banco da Liga: clube invisível vinculado à primeira liga

## Segurança inicial

As senhas não são gravadas em texto puro. A Sprint utiliza PBKDF2 com salt aleatório, chave de 256 bits e comparação em tempo constante. A senha inicial é temporária e deverá ser substituída no primeiro fluxo de autenticação.

## Integridade

- O administrador é localizado por nome de usuário antes da criação.
- A liga é localizada por nome antes da criação.
- O Banco da Liga é localizado pelo campo `isBank` antes da criação.
- Todo o processo ocorre em uma única transação do banco.
- Restrições únicas do Room continuam funcionando como proteção adicional.

## Validação

A análise estática dos arquivos foi concluída. A compilação não pôde ser finalizada neste ambiente porque o Gradle Wrapper tentou acessar `services.gradle.org`, que não estava disponível. Execute `gradlew.bat :app:assembleDebug` no Android Studio/Windows com internet para confirmar o build.
