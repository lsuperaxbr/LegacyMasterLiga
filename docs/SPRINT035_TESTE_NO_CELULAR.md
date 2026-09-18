# Sprint 035 — Primeiro teste no celular

Este roteiro começa **depois** que o APK Beta for gerado no computador.

## 1. Localizar o APK

O arquivo esperado é:

```text
app\build\outputs\apk\beta\app-beta.apk
```

Também é possível executar no Windows:

```bat
scripts\package_beta_for_phone.bat
```

O script copia o APK para uma pasta mais fácil de encontrar:

```text
beta-package\LegacyMasterLiga-1.0.0-beta06.apk
```

## 2. Enviar o APK ao celular

Use um destes meios:

- cabo USB;
- Google Drive;
- WhatsApp para você mesmo;
- Telegram;
- Bluetooth.

Não envie o ZIP do projeto ao celular. Envie somente o arquivo `.apk`.

## 3. Permitir a instalação

No Android, abra o APK. Caso apareça um bloqueio:

1. toque em **Configurações**;
2. permita **Instalar apps desconhecidos** para o aplicativo usado para abrir o APK;
3. volte e toque em **Instalar**.

Essa autorização vale apenas para a fonte escolhida e pode ser desativada depois.

## 4. Abrir a Beta

O nome da variante de teste pode aparecer com o sufixo Beta. Ela pode ser instalada ao lado de outras variantes porque usa um identificador próprio.

## 5. Login inicial

Administrador:

```text
Usuário: admin
Senha: admin123
```

Presidentes de demonstração:

```text
Tomascote: tomascote / demo123
Richemont: richemont / demo123
Pipocacr7: pipocacr7 / demo123
LSuperax: lsuperax / demo123
```

## 6. Ordem recomendada do primeiro teste

1. Entrar como Administrador.
2. Confirmar a Liga M L Amigos.
3. Abrir Clubes e conferir os quatro clubes.
4. Abrir Competições e confirmar Liga Principal / Temporada 1.
5. Gerar ou conferir rodadas.
6. Lançar um placar.
7. Conferir a classificação e o líder amarelo.
8. Registrar uma transferência simples.
9. Conferir Financeiro, Notícias e Auditoria.
10. Criar um backup manual.
11. Sair e entrar como Presidente.
12. Confirmar as permissões limitadas.

## 7. Não testar ainda

- Não use dados reais importantes antes de validar o backup.
- Não distribua o APK para todo o grupo antes do primeiro teste controlado.
- Não altere arquivos internos do projeto para tentar corrigir erros.
