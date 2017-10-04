(ns edsger.parsing
  "Tools to convert user input into cljs lists"
  (:require [instaparse.core :as insta]))

(def ^:private lisp-style-cfg
  "Lisp-like syntax parser for logic expression"
  (insta/parser
   "S = E | D | '('B' 'S')' | '('C' 'S' 'S')'
    E = 'a' | 'b' | 'c'
    D = 'true' | 'false'
    B = 'not'
    C = 'and' | 'or' | 'equiv'"))

;; TODO use the insta/parser macro to do more of the work at build time.
(def infix-cfg
  "A CFG for a simple logic supporting several logic operators.
   Currently requires expressions to be fully parenthesized"
  (insta/parser
   (str "top-level   = boolean | variable | unary-expr | binary-expr;"
        "boolean     = 'true' | 'false';"
        "variable    = #'[a-zA-Z]';" ;; we only support single-character variables
        "unary-op    = '¬';"
        "unary-expr  = unary-op (<' '> | epsilon) bottom;"
        "bottom      = boolean | variable | <'('> top-level <')'>;"
        "binary-expr = bottom <' '> binary-op <' '> bottom;"
        "binary-op   = '∨' | '∧' | '≡'| '⇒';"
        )))

(def ^:private operator-map
  {"¬" :not
   "∨" :or
   "∧" :and
   "≡" :equiv
   "⇒" :implies})

(defn- transform-infix-cfg
  "When given a hiccup tree produced produced by infix-cfg,
   returns the tree structure in the prefix list style that
   we prefer to work with."
  [parse-tree]
  (insta/transform
   {:boolean #(= "true" %)
    :variable symbol
    :unary-op #(get operator-map %)
    :binary-op #(get operator-map %)
    :unary-expr (fn [op exp] [op exp])
    :binary-expr (fn [left op right] [op left right])
    :bottom identity
    :top-level identity}
   parse-tree))

(defn mk-list
  "Takes in a tree generated by lisp-style-cfg and
   returns a simple list of lists representing the expression."
  [hiccup-tree]
  (case (first hiccup-tree)
    :S (let [filtered-list (filter vector? hiccup-tree)] ;; spaces and parens are ignored
         (if (= 1 (count filtered-list))
           (mk-list (first filtered-list)) ;; constant case
           (map mk-list (filter vector? hiccup-tree)))) ;; nested structure
    :E (symbol (second hiccup-tree))
    :D (= "true" (second hiccup-tree))
    :B (keyword (second hiccup-tree))
    :C (keyword (second hiccup-tree))))

(defn parse
  "Takes a stringfied expression and converts it into
   a legitimate expression. Returns nil when the expression
   cannot be parsed."
  [expression]
  (transform-infix-cfg (infix-cfg expression)))

(defn rulify
  "Recursively traverse a list and prepend '?' onto
   all symbols."
  [input]
  (map
   (fn [element]
     (cond
       (list? element) (rulify element)
       (symbol? element) (symbol (str "?" element))
       :else element))
   input))

(print "START in parsing")



;; (print (as-and-bs "(and (a) (b))"))
;; (print (count (as-and-bs "(and (a) (b))")))
;; (print (type (as-and-bs "(and (a) )")))

(print "ENND in parsing")
