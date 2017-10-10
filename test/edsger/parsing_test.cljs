(ns edsger.parsing-test
  (:require [edsger.parsing :as p]
            [clojure.test :as t :refer-macros [deftest is are] :include-macros true]))


;; ==== Tests for `infix-cfg`

(deftest infix-cfg_single-value-expression
  (are [input expected-output] (= (p/infix-cfg input) expected-output)
    "true" [:top-level [:boolean "true"]]
    "false" [:top-level [:boolean "false"]]
    "a" [:top-level [:variable "a"]]
    "m" [:top-level [:variable "m"]]
    "z" [:top-level [:variable "z"]]))

(deftest infix-cfg_one-level-operations
  (are [input expected-output] (= (p/infix-cfg input) expected-output)
    "¬ true" [:top-level [:unary-expr [:unary-op "¬"] [:bottom [:boolean "true"]]]]
    "a ⇒ false" [:top-level [:binary-expr
                             [:bottom [:variable "a"]]
                             [:binary-op "⇒"]
                             [:bottom [:boolean "false"]]]]
    "p ≡ q" [:top-level [:binary-expr
                         [:bottom [:variable "p"]]
                         [:binary-op "≡"]
                         [:bottom [:variable "q"]]]]
    "t ∧ u" [:top-level [:binary-expr
                         [:bottom [:variable "t"]]
                         [:binary-op "∧"]
                         [:bottom [:variable "u"]]]]))

(deftest infix-cfg_complex-nexting-with-parens-works
  (is (=
       (p/infix-cfg "(a ⇒ false) ∨ (¬ (t ∧ u))")
       [:top-level [:binary-expr
                    [:bottom
                     [:top-level
                          [:binary-expr
                           [:bottom [:variable "a"]]
                           [:binary-op "⇒"]
                           [:bottom [:boolean "false"]]]]]
                    [:binary-op "∨"]
                    [:bottom
                     [:top-level
                          [:unary-expr
                           [:unary-op "¬"]
                           [:bottom
                                [:top-level
                                 [:binary-expr
                                  [:bottom [:variable "t"]]
                                  [:binary-op "∧"]
                                  [:bottom [:variable "u"]]]]]]]]]])))

(deftest infix-cfg_spaces-with-not
  (are [input] (= (p/infix-cfg input) [:top-level
                                       [:unary-expr
                                        [:unary-op "¬"]
                                        [:bottom [:boolean "true"]]]])
    "¬ true"
    "¬true"))


;; ==== Tests for `transform-infix-cfg`

(deftest transform-infix-cfg_works-with-infix-cfg
  (are [input expected-output] (= (p/transform-infix-cfg (p/infix-cfg input)) expected-output)
    "true" true
    "false" false
    "a" 'a
    "m" 'm
    "z" 'z
    "¬ true" [:not true]
    "a ⇒ false" [:implies 'a false]
    "p ≡ q" [:equiv 'p 'q]
    "t ∧ u" [:and 't 'u]
    "(a ⇒ false) ∨ (¬ (t ∧ u))" [:or
                                 [:implies 'a false]
                                 [:not [:and 't 'u]]]))


;; ==== Tests for `parse`

(deftest parse_good
  (are [input output] (= (p/parse input) output)
    "a ∨ (b ∧ c)" '(:or a (:and b c))
    "¬ false" '(:not false)))

(deftest parse_bad
  (is (nil? (p/parse "(:or a)"))))


;; ==== Tests for `rulify`

(deftest rulify_works-correctly
  (is (= '(:and ?b ?c (:or (:not ?a) ?c))
         (p/rulify '(:and b c (:or (:not a) c)))))
  (is (= '(:and true ?c (:or (:not ?a) false))
         (p/rulify '(:and true c (:or (:not a) false)))))
  (is (= '?q
         (p/rulify 'q)))
  (is (= 10
         (p/rulify 10))))
