(ns edsger.unification-macros
  (:require [cljs.core.logic :as logic :refer [run* fresh ==]]))


(defn get-symbols
  "Recursively traverse the passed in structures and
   return a set of all the unique symbols therein."
  [& structures]
  (set (filter symbol?
               (flatten structures))))

(defmacro match-rule [expression rule symbols]
  "Return bindings for the free variables in rule that will
   cause rule to be equal to expression. Currently, any
   symbol is treated as a free variable."
  `(run* [~'q]
     (fresh
       ~(conj (vec symbols) 'expression-sym 'rule-sym)
       (== ~'expression-sym ~expression)
       (== ~'rule-sym
           ~(if (symbol? rule)
              rule
              `(list ~@rule)))
       ;; (if (= 1 (count stripped))
       ;;   (first  stripped)
       ;;   `(list ~@stripped))))
       (== ~'expression-sym ~'rule-sym)
       (== ~'q ~(vec symbols)))))
