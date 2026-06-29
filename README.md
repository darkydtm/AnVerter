# Anverter

Конвертер фиатных и криптовалют со встроенным калькулятором для Android, оформленный в стиле **MiUIX / HyperOS**.

## Возможности

- 💱 Конвертация **фиат ↔ крипта ↔ фиат** (всё через единый запрос к CoinGecko, привязка к BTC).
- 🧮 Встроенный калькулятор (выражения с приоритетом операций и скобками).
- 🎨 UI в стиле MIUI на библиотеке [miuix](https://github.com/compose-miuix-ui/miuix), Material You с динамическими цветами (Android 12+).
- 🔌 **Никакой фоновой работы.** Курсы синхронизируются только когда приложение открыто и есть сеть. Последние курсы кэшируются для работы offline.

## Архитектура

Single-Activity Jetpack Compose. Чистая логика (`Converter`, `CalculatorEngine`) изолирована от Android и покрыта unit-тестами. Сеть — OkHttp + kotlinx.serialization, кэш — DataStore. DI вручную через `AppContainer`.

Источник курсов: `https://api.coingecko.com/api/v3/exchange_rates` — один запрос отдаёт курсы фиатов, крипты и металлов относительно BTC. Любая пара `X → Y` считается как `amount * rate[Y] / rate[X]`.

## Сборка

Сборка выполняется **только в GitHub Actions** (см. `.github/workflows/build.yml`): параллельная matrix-сборка `debug` и `release` с кэшированием Gradle, configuration cache и dependency cache. Артефакты APK публикуются в каждом запуске.

Локальная сборка (опционально):

```bash
./gradlew assembleDebug
```

## Лицензия

[GNU GPL v3.0](./LICENSE) © Anverter contributors.
