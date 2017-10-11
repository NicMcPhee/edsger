edsger: _Simple proof checker for introductory logic_
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
git clone https://github.com/NicMcPhee/edsger
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
you need to type the expressions and rules in pseudo-clojure style.  

:warning: In the next version of edsger, the input format will be 
changed to look similar to the actual expression above.
```
first box  : (and a (or c b))
second box : (or a b)
third box  : (or b a)
fourth box : (and a (or b c))
```
And, click the **validate** button to check your reasoning :100:.

## License
TBA soon
