edsger: _Simple proof checker for introductory logic_ 
[![Build Status](https://travis-ci.org/UMM-CSci/edsger.svg)](https://travis-ci.org/UMM-CSci/edsger)
=====================================================

## Introduction

The idea is to provide a simple checker for basic logic proofs that students
generate in the sort of discrete math/logic course that is often required in
Computer Science programs. In these courses, students often struggle with the
basic question of "is this correct?" in ways that don't come up in an intro
programming course _because there the compiler/interpreter gives them hard
feedback_. So while students in an intro programming course are forced to face
confusion about things like syntax right away, in our logic/discrete course
students can flail on basic syntactic issues for weeks, and are often very
frustrated because they just don't know if "they've done it right".

This project is a web-based ClojureScript tool that will allow students to check
the correctness of each step in their proofs, at least for simple propositional
calculus. The basic functionality is in place, but there is a lot of work to be
done before this will be a tool that students will want to use for checking more
than a few steps of a proof.

## Quick Start

See the [dev guide](doc/developer/dev-guide.md) for more info about how the project is set up.

Clone and run this repository to see the app in action
```
git clone https://github.com/UMM-CSci/edsger.git
cd edsger
lein figwheel
```
See [this page](doc/developer/hacking.md) for instructions on making figwheel work with Spacemacs.

### Running the tests

```
npm install
PATH="$(pwd)/node_modules/karma-cli/bin:$PATH"
lein doo chrome test once
```

These steps assume you have Chrome. If want to run the tests in Firefox, simply
replace `chrome` with `firefox`.

Tips:
- If you omit `once` from the end of the command, the tests be re-run every time
  that Karma detects a change in the compiled JS files. Very handy when developing
  tests.
- Check out [direnv](https://direnv.net/) if you want to avoid manually setting your
  `PATH`.
- If you wish to avoid opening a browser window
  for the tests, use `chrome-headless` instead (requires Chrome 59 or later).

## Usage & Current State

Users enter a starting expression, a rule they wish to use, and the ending expression. 
To cite a rule, the rule must be given in two pieces: a left-hand rule that 
matches the expression we are starting from, and a
right-hand rule that matches the expression we end up with.

So, when you validate a logic expression looking like:  
```
  a ∧ (c ∨ b)
≣    <(3.24) Symmetry of ∨>
  a ∧ (b ∨ c) 
```
you need to type the expressions and rules similar to the above expression.  

```
top-expression box:     | a ∧ (b ∨ c) |
left-rule box:     | p ∨ q |  ≣  | q ∨ p |   :right-rule box
bottom-expression box : | a ∧ (c ∨ b) |
```

:interrobang: To type logic symbols like `≣` or `∨`, you don't have to copy them 
from somewhere. _edsger_ is doing conversion for you when you type Java-like symbols. 
Below is the conversion table.

| Logic Symbol    | Regular Symbol (on keyboard) |
| -------------   | -------------                |
| ¬ (not)         | ! or "not"                   |
| ∧ (and)         | &, ^, or "and"               |
| ∨ (or)          | \| or "or"                   |
| ⇒ (implication) | =>, ->, or "implies"         |
| ≡ (equivalence) | == or "equiv"                |

Click the + button to add steps to your proof, and the - button if you accidentally add too many. 
_edsger_ will not run if any input box is empty.

Finally, click the **validate** button to check your reasoning :100:.

The logic symbols given in the table above are the only ones that we currently
support. The parser has knowledge of precedence, but series of operators with
equal precendence need to be made unambigious with parens. We see this in the
example above, but another example would be `a ∧ b ∧ c`, which would not parse
unless you add parens to make either `(a ∧ b) ∧ c` or `a ∧ (b ∧ c)`.

## License

Distributed under the Eclipse Public License version 2.0.
