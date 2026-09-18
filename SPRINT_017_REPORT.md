# Sprint 017 — Configurações avançadas

## Objetivo
Implementar configurações administrativas da liga, regras de pontuação e desempate por competição e preferências visuais do aplicativo.

## Entregas
- Tela administrativa de Configurações avançadas.
- Alteração segura do nome da liga, mantendo a moeda oficial CR.
- Pontuação configurável por competição para vitória, empate e derrota.
- Critérios de desempate persistidos por competição.
- Opção para manter o líder destacado em amarelo.
- Preferências de tema: sistema, escuro e claro.
- Preferência de densidade visual e ativação de animações, preparadas para uso gradual nas telas.
- Tema claro e aplicação reativa do tema sem reiniciar o aplicativo.
- Auditoria automática das mudanças de configurações.
- Integração com Dashboard, Navigation Compose, Room e Hilt.

## Banco de dados
- `AppSettingsEntity`
- `CompetitionSettingsEntity`
- `SettingsDao`
- Migração Room 8 → 9
- Versão do banco: 9

## Versão
- versionCode: 17
- versionName: 0.17.0-alpha17

## Validação
- Estrutura e referências revisadas estaticamente.
- A compilação automática depende do download do Gradle Wrapper em ambiente com acesso à internet.
