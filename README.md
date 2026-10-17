# MandaGram SDK — Kotlin/Native

SDK для авторов нативных плагинов MandaGram на Kotlin. Привязки ко всему C API генерируются из канонического заголовка; отдельный пример Plain Text собирается в DLL для Windows x64. Это Kotlin/Native, не JVM-плагин.

**Важно:** текущий жизненный цикл загрузки и выгрузки плагинов MandaGram несовместим со встроенным runtime Kotlin/Native. Готовая DLL не означает, что её безопасно устанавливать в текущий клиент. [Ограничения и порядок изолированной проверки](docs/building.md#жизненный-цикл-и-безопасность).

- [Подключение SDK, сборка и пример](docs/building.md)
- [Ревизия API и SHA256](sdk-source.json)
- [MandaGram](https://github.com/GrapeS-T/MandaGram)
- [База знаний](https://mandagram.com/kb/)
