# Kotlin Flow拡張ライブラリ

[![Build checks](https://github.com/gahojin/kflowext/actions/workflows/build.yml/badge.svg)](https://github.com/gahojin/kflowext/actions/workflows/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/jp.co.gahojin.kflowext/kflowext)](https://central.sonatype.com/artifact/jp.co.gahojin.kflowext/kflowext)
[![GitHub License](https://img.shields.io/github/license/gahojin/kflowext)](LICENSE)

Kotlin Coroutinesに含まれるFlowを拡張するライブラリです


## Functions

### bufferTimeout

RxJSのbufferTimeに類似した機能となり、受信した値をバッファに収集し、バッファが最大サイズに達するか、またはmaxTimeが経過するたびに、バッファを返すオペレータです

### throttleFirst

RxJSのthrottleFirstに類似した機能となり、値を受信してから、一定期間経過するまで、受信をブロックするオペレータです

 
## License

```
Copyright 2026, GAHOJIN, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
