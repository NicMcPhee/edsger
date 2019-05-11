(ns edsger.unification
  (:require [cljs.core.logic :as logic :refer [binding-map]]))

(defn- wrap [exp]
  (if-not (list? exp)
    (list exp)
    exp))

(defn check-match [start-exp end-exp start-rule end-rule]
  "Returns true if start-exp can be matched with start-rule
   with the same bindings that cause end-exp to match end-rule."
  (let [start-matches (binding-map
                       (wrap start-exp)
                       (wrap start-rule))
        end-matches (binding-map
                     (wrap end-exp)
                     (wrap end-rule))]
    (and
     start-matches
     end-matches
     (every? (fn [[key end-binding]]
               (let [start-binding (get start-matches key)]
                 (or (nil? start-binding) ;; constants are matched
                     (= start-binding end-binding)))) ;; substitutions are matched
             end-matches))))


(defn check-match-recursive [start-exp end-exp start-rule end-rule]
  "Returns true when the given rules can be used to make the start-exp
   equal to the end-exp."
  (let [check (fn [s e]
                (check-match s e start-rule end-rule))]
      (or
        (check start-exp end-exp)
        (and
         ;; if start-exp and end-exp don't pass the check as a whole,
         ;; then we must be able to break them down to find a place
         ;; to apply the rule
         (seqable? start-exp)
         (seqable? end-exp)
         ;; only works for the same length exps
         (= (count start-exp) (count end-exp))
         (let
            [result-list (map (fn [s e]
                                {:equal (= s e)
                                 :match (if (and (list? s) (list? e))
                                          (check-match-recursive s e
                                                                 start-rule
                                                                 end-rule)
                                          (check s e))})
                              start-exp
                              end-exp)]
          (and
           (some :match result-list)
           (every? #(or (:match %) (:equal %)) result-list)))))))

(defn recursive-validate
    "validates each step in order, returning an array of boolean values corresponding to the rules, in order"
     [exps rules steps]
       (cond
             ;if one expression and 0 rules are left, all expressions and rules validated
             (and (= 1 (count exps)) (empty? rules)) '()
             ;if only one list is empty, something is wrong
             (or (>= 1 (count exps)) (empty? rules)) (throw (js/Error. "Mismatched expression and rules lists' lengths"))
             (and (= (first steps) "⇒") (check-match (nth exps 0) (nth exps 1) (nth rules 0) (nth rules 1)))
                (cons true (recursive-validate (rest exps) (rest (rest rules)) (rest steps)))
             (and (= (first steps) "≡")
                  (true? (check-match-recursive (nth exps 0) (nth exps 1) (nth rules 0) (nth rules 1))))
                (cons true (recursive-validate (rest exps) (rest (rest rules)) (rest steps)))
             ;if check-match-recursive didn't return true, end the computation
             :else (cons false (recursive-validate (rest exps) (rest (rest rules))))))


(defn check-implication
  [exps rules]
    (cond
      ;if one expression and 0 rules are left, all expressions and rules validated
      (and (= 1 (count exps)) (empty? rules)) '()
      ;if only one list is empty, something is wrong
      (or (>= 1 (count exps)) (empty? rules)) (throw (js/Error. "Mismatched expression and rules lists' lengths"))
      (true? (check-match (nth exps 0) (nth exps 1) (nth rules 0) (nth rules 1)))
                      (cons true (check-implication (rest exps) (rest (rest rules))))
      ;if check-match didn't return true, cons a false on and keep going
      :else (cons false (check-implication (rest exps) (rest (rest rules))))))
;; This file is currently only for playing around during development
;; but I think that we'll eventually have some useful functions here.

;; When this file is evaluated, the
;; code here is executed and anything that is (print)-ed will
;; be printed in the developer console in Chrome when using figwheel.
;; This means we can just make a change, save, and then switch to
;; Chrome to see what printed.

(print "BBORK!\nPRE") ;; I like a header for the section

(def a '(+ ?x 1))
(def b '(+ 1 1))

(def exp-start '(:not (:equiv u (:or w y))))
(def exp-end '(:equiv (:not u) (:or w y)))
(def lhs '(:not (:equiv ?a ?b)))
(def rhs '(:equiv (:not ?a) ?b))

(print (binding-map exp-start lhs))
(print (binding-map exp-end rhs))
(print (= (binding-map exp-start lhs)
          (binding-map exp-end rhs)))



(print "POST\nBBORK!") ;; I like a footer for the section
