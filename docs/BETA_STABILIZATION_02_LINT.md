# Beta Stabilization 02 — Android Lint

## Objetivo

Executar o Android Lint da variante Beta e revisar acessibilidade, Manifest, APIs obsoletas, internacionalização, Compose e segurança sem alterar as regras do aplicativo.

## Executar no Windows

```bat
scripts\run_lint_beta.bat
```

O log será criado em:

```text
build-reports\beta-stabilization-02-lint.log
```

Os relatórios HTML/XML/SARIF serão gerados pelo Android Gradle Plugin dentro de `app/build/reports/`.

## Verificação offline

Quando o Gradle não estiver disponível:

```bat
python scripts\lint_static_review.py
```

A verificação offline não substitui o Android Lint. Ela confirma Manifest, strings, política de backup, tráfego seguro e ausência de código temporário, além de listar pontos que ainda merecem revisão.

## Decisões desta estabilização

- `GradleDependency` e `NewerVersionAvailable` permanecem desativados no Lint durante a estabilização, pois atualizações de dependências não fazem parte desta fase.
- Erros reais continuam bloqueando o Lint; avisos são relatados sem quebrar o build Beta.
- O aplicativo declara `pt-BR` como idioma atual.
- A tela de Login e os estados visuais compartilhados passaram a usar recursos em `strings.xml`.
- Ícones com `contentDescription = null` permanecem apenas quando decorativos ou acompanhados por texto/semântica equivalente; o relatório offline lista a quantidade para revisão contínua.
