# Checklist de Entrega — Beta 1

## Projeto

- [x] Versão definida como `1.0.0-beta02`.
- [x] Variante `beta` criada.
- [x] Configuração opcional de assinatura Release.
- [x] Arquivo de regras ProGuard presente.
- [x] Backup automático nativo do Android desativado.
- [x] Tráfego HTTP sem criptografia bloqueado.
- [x] Testes automatizados presentes.
- [x] Script de verificação estática presente.

## Antes de distribuir

- [ ] Sincronizar o Gradle no Android Studio.
- [ ] Executar os testes unitários.
- [ ] Executar os testes instrumentados em aparelho ou emulador.
- [ ] Executar lint.
- [ ] Gerar `app-beta.apk`.
- [ ] Instalar em um celular real.
- [ ] Executar o plano de testes.
- [ ] Criar backup de uma liga de teste.
- [ ] Registrar e corrigir os problemas encontrados.

## Release futuro

- [ ] Criar e proteger o keystore.
- [ ] Configurar `keystore.properties`.
- [ ] Habilitar e validar R8 após a Beta.
- [ ] Gerar APK/AAB assinado.
