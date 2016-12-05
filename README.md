# Rondel

[![Build Status](https://travis-ci.org/blazsolar/rondel.svg?branch=master)](https://travis-ci.org/blazsolar/rondel)
[![Coverage Status](https://coveralls.io/repos/github/blazsolar/rondel/badge.svg)](https://coveralls.io/github/blazsolar/rondel)

Rondel is an annotation processor that makes use of [Dagger 2](http://google.github.io/dagger/) easier on Android. 

By introudcing simple API rondel adds support for simply binding your Application, Activities, Fragments and/or Views with Dagger components.

While Rondel makes it easy to deal with Dagger on Android id doesn't restrict you. At any time, anywhere in you code structure you can still use pure Dagger syntacs. This way you can easly work around limitations that currently exist in Rondel. The reason behnd that is that Rondel generates code that you would otherwise write by your self. That code is then passed to Dagger processor the same what that is when you write the code. 

## Limitations

At the moment there are still a few limitations when using Rondel.

 * Views can not depend on Fragments as parent.
 * No extra parameters can't be send to modules.

## License

```
Copyright 2016 Blaž Šolar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```