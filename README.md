# Anverter

A fiat and cryptocurrency converter with a built-in calculator for Android, styled in the MiUIX look.

## Features

- 💱 Convert **fiat ↔ crypto ↔ fiat** (everything through a single CoinGecko request, anchored to BTC).
- 🧮 Built-in calculator (expressions with operator precedence and parentheses).
- 🎨 MiUIX-style UI built on the [miuix](https://github.com/compose-miuix-ui/miuix) library, with Material You dynamic colors (Android 12+).
- 🔌 **No background work.** Rates sync only while the app is open and online. The latest rates are cached for offline use.
- 🌍 Localized into 10 popular languages.

## Architecture

Single-Activity Jetpack Compose. The pure logic (`Converter`, `CalculatorEngine`) is isolated from Android and covered by unit tests. Networking uses OkHttp + kotlinx.serialization, caching uses DataStore, and dependency injection is done manually through an `AppContainer`.

Rates source: `https://api.coingecko.com/api/v3/exchange_rates` - a single request returns fiat, crypto and commodity rates relative to BTC. Any pair `X -> Y` is computed as `amount * rate[Y] / rate[X]`.

## License

This project is licensed under the [GNU GPL v3.0](./LICENSE) © Anverter contributors.

It bundles the [miuix](https://github.com/compose-miuix-ui/miuix) UI library, which is distributed under the [Apache License 2.0](https://github.com/compose-miuix-ui/miuix/blob/main/LICENSE).
