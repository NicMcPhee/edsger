edsger: _Simple proof checker for introductory logic_ 
[![Build Status](https://travis-ci.org/UMM-CSci/edsger.svg)](https://travis-ci.org/UMM-CSci/edsger)
=====================================================

## Introduction

The idea is to provide a simple checker for basic logic proofs that students
generate in the sort of discrete math/logic course that is often required in
Computer Science programs. In these courses, students often struggle with the
basic question of "is this correct?" in a ways that don't come up in an intro
programming course _because there the compiler/interpreter gives them hard
feedback_. So while students in an intro programming course are forced to face
confusion about things like syntax right away, in our logic/discrete course
students can flail on basic syntactic issues for weeks, and are often very
frustrated because they just don't know if "they've done it right".

Our goal here is to write a web-based ClojureScript tool that will allow
students to check the correctness of each step in their proofs, at least for
simple propositional calculus. It's likely to be _very_ simple, at least until
there's some evidence that the students find it useful.

## Quick Start

:warning: edsger is still under active development. The running instructions are most likely to change in the future.

Clone and run this repository to see the app in action
```
git clone https://github.com/UMM-CSci/edsger.git
cd edsger
lein figwheel
```

## Usage

When you validate a logic expression looking:  
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
The below is the conversion table.

| Logic Symbol    | Regular Symbol (on keyboard) |
| -------------   | -------------                |
| ¬ (not)         | !                            |
| ∧ (and)         | &                            |
| ∨ (or)          | \|                           |
| ⇒ (implication) | =>                           |
| ≡ (equivalence) | ==                           |

And finally, click the **validate** button to check your reasoning :100:.

## Running the tests

Running the tests requires Karma. You can install Karma and all the needed
plugins by running `npm install` in the root of the repo. You'll then need to
add `./node_modules/karma-cli/bin` to your
`$PATH`. Check out [direnv](https://direnv.net/) if you want to make this easier
to do.

If you have Google Chrome, you can run the tests once with the following command:

```
lein doo chrome test once
```

If want to run the tests in Firefox, simply replace `chrome` with `firefox`. If
you wish to avoid opening a browser window for the tests, use `chrome-headless`
instead (requires Chrome 59 or later).

If you omit `once` from the end of the command, the tests be re-run every time
that Karma detects a change in the compiled JS files.

## License

Distributed under the Eclipse Public License either version 1.0 or any later version.
