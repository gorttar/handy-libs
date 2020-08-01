# Handy libraries
Bunch of somewhat usable libraries without any particular direction.

Approximate structure:
* `org.gorttar.control` - declarations of custom control structures
  * [managed value](src/main/kotlin/org/gorttar/control/ManagedValue.kt) - provides a way to run a block of code `on` some
  stateful value preserving its old state before execution and restoring it after. May be useful for temporarily changing some global environment such as system in/out console streams, root logger log level etc.
* `org.gorttar.data` - declarations of custom data structures/operations on them
  * [sequence equality](src/main/kotlin/org/gorttar/data/SequenceEquality.kt) - `fun eq` checks two sequences for structural equality with the following limitations/behavior details:
    * it actually iterates over sequences so them would better be able to iterate multiple times
    * it can't check infinite equal sequences for equality but can determine their inequality
  * `org.gorttar.data.heterogeneous.list` - immutable heterogeneous list implementation inspired by [Strongly Typed Heterogeneous Collections](http://okmij.org/ftp/Haskell/HList-ext.pdf) article
    * `org.gorttar.data.heterogeneous.list.generators` - bunch of utility classes to generate boilerplate code
    * [Aliases.kt](src/main/kotlin/org/gorttar/data/heterogeneous/list/Aliases.kt) - auto generated file with type aliases intended to shorten list signatures
    * [Destructuring.kt](src/main/kotlin/org/gorttar/data/heterogeneous/list/Destructuring.kt) - auto generated file declaring `componentN` operators for lists up to 26 elements
    * [ExtensionProperties.kt](src/main/kotlin/org/gorttar/data/heterogeneous/list/ExtensionProperties.kt) - auto generated file declaring `a..z` extension properties for lists up to 26 elements
    * [HList.kt](src/main/kotlin/org/gorttar/data/heterogeneous/list/HList.kt) - main data structure declaration
    * [Literals.kt](src/main/kotlin/org/gorttar/data/heterogeneous/list/Literals.kt) - auto generated file declaring `hListOf` factory functions for lists up to 26 elements
* `org.gorttar.graphics.turtle` - simple [turtle graphics](https://en.wikipedia.org/wiki/Turtle_graphics) library for Kotlin. See [Koch flake](src/main/kotlin/org/gorttar/graphics/turtle/AwtHello.kt) example for more details
* [`org.gorttar.repr.repr`](src/main/kotlin/org/gorttar/repr/Repr.kt) - reflective `String` representation of arbitrary objects
* `org.gorttar.test.io` - simple [expect](https://en.wikipedia.org/wiki/Expect) like library. Still WIP