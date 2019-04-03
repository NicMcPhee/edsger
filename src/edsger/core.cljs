(ns edsger.core
  "Front-end UI controller"
  (:require [clojure.browser.repl :as repl]
            [clojure.browser.dom  :as dom]
            [goog.events :as events]
            [goog.dom :as gdom]
            [goog.dom.selection :as gselection]
            [edsger.unification :as uni]
            [edsger.parsing :as parsing]))

(enable-console-print!)

;; Constants =========================

; copy of div for rule input
(def rule-div "<div class=\"form-group row rule-box\">
       <label for=\"inputRule\" class=\"col-sm-2 col-form-label rule-label\">Rule</label>
       <div class= \"result-val\"></div>
       <div class=\"col rule-box-left\">
           <input type=\"text\" class=\"form-control rule\" placeholder=\"Left-hand side of rule\">
       </div>
       <span> ≡ </span>
       <div class=\"col rule-box-right\">
           <input type=\"text\" class=\"form-control rule\" placeholder=\"Right-hand side of rule\">
       </div>
   </div>")

; copy of div for expression input
(def exp-div "<div class=\"form-group row exp-box\">
       <label for=\"inputExp\" class=\"col-sm-2 col-form-label\">Expression</label>
       <div class=\"col\">
           <input type=\"text\" class=\"form-control ex\" placeholder=\"Type an expression (e.x. q ∧ p)\">
       </div>
   </div>")
;; Helpers ===========================

;; shortcut for dom/get-element
(defn- by-id [id] (dom/get-element id))

;; shortcut for dom/element
(defn- elemt
  ([tag-or-text]
   (dom/element tag-or-text))
  ([tag params]
   (dom/element tag params)))

(defn- iArrayLike-to-cljs-list
  "Converts goog's iArrayLike type to cljs list"
  [iArr]
  (let [length (aget iArr "length")]
    (for [i (range length)]
      (aget iArr i))))

(defn- str-to-elem
  "Generates a HTML element node from the given
   HTML-looking string (e.g., \"<span>Yo</span>\")"
  [html-str]
  (.createContextualFragment (.createRange js/document) html-str))

(defn- remove-elems-by-class
  "Removes all elements from dom with the given class"
  [class]
  (dorun (map #(gdom/removeNode %) (iArrayLike-to-cljs-list (gdom/getElementsByClass class)))))



;; UI and handlers ===================

;; Bootstrap alert div
(def parse-err-str
  (str "<div class=\"alert alert-danger alert-dismissible fade show col-2.5\" role=\"alert\">"
         "<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\">"
           "<span aria-hidden=\"true\">&times;</span>"
         "</button>"
         "Error in input"
       "</div>"))

(defn- merge-val
  "Takes a map looking `{:curr-id # :locations []}` and a number.
   Returns a new map looking `{:curr-id (inc #) :locations []}`.
   If val is nil, (inc #) is added to the `:locations` vector"
  [curr-map val]
  (let [curr-map (update curr-map :curr-id inc)
        curr-id (:curr-id curr-map)]
    (if (nil? val) (update-in curr-map [:locations] #(conj % curr-id)) curr-map)))

(defn- show-exp-parse-err
  "Takes a vector of indices and attach error message elements
   to corresponding exp input box"
  [err-id-vec]
  (let [exp-boxes (iArrayLike-to-cljs-list (gdom/getElementsByClass "exp-box"))]
    (dorun
     (map
      (fn [id]
        ;; adding err msg where they fail
        (gdom/appendChild (nth exp-boxes id) (str-to-elem parse-err-str)))
      err-id-vec))))

(defn- show-rule-parse-err
  "Takes a vector of indices and attach error message elements
   to corresponding rule input box"
  [err-id-vec]
  (let [rule-cols (map #(gdom/getParentElement %)
                       (iArrayLike-to-cljs-list (gdom/getElementsByClass "rule")))]
    (dorun
     (map
      (fn [id]
        ;; adding err msg where they fail
        (gdom/insertSiblingAfter (str-to-elem parse-err-str) (nth rule-cols id)))
      err-id-vec))))

(defn show-results
  [results]
  (dorun (map
          (fn [old-result result]
            (gdom/replaceNode (str-to-elem (str "<div class='result-val'>" result "</div>")) old-result))
          (iArrayLike-to-cljs-list (gdom/getElementsByClass "result-val"))
          results)))

(defn validate-imply-or-equiv
  [proof-type exps rules]
  (if (= proof-type "⇒")
    (uni/recursive-validate-imply exps rules)
    (uni/recursive-validate-equiv exps rules)))

(defn validate-handler
  "Performs the validation based on the values typed by users"
  (let [exp-str-li (map #(.-value %) (iArrayLike-to-cljs-list (gdom/getElementsByClass "ex")))
        rule-str-li (map #(.-value %) (iArrayLike-to-cljs-list (gdom/getElementsByClass "rule")))
        non-empty-input (every? #(not= "" %) (concat exp-str-li rule-str-li))
        exps (map #(parsing/parse %) exp-str-li)
        vanilla-rules (map #(parsing/parse %) rule-str-li)
        rules (map #(parsing/rulify %) vanilla-rules)
        ;; vector of indices where parsing err occurred (e.g., [0, 3])
        exp-parse-err (:locations (reduce merge-val {:curr-id -1 :locations []} exps))
        rule-parse-err (:locations (reduce merge-val {:curr-id -1 :locations []} vanilla-rules))
        ;result-str (str (true? (and (uni/check-match-recursive (nth exps 0)
      ;                                                  (nth exps 1)
      ;                                                    (nth rules 0)
      ;                                                    (nth rules 1))
      ;                              (ugeni/check-match-recursive (nth exps 1)
      ;                                                    (nth exps 2)
      ;                                                    (nth rules 2)
      ;                                                    (nth rules 3)))))
        proof-type (gdom/getTextContent (by-id "type-proof"))
        results (uni/recursive-validate-imply exps rules)]
    (remove-elems-by-class "alert-danger")
    (when non-empty-input
      (if (some not-empty [exp-parse-err rule-parse-err])
        (do (show-exp-parse-err exp-parse-err)
            (show-rule-parse-err rule-parse-err))
        ;(.alert js/window results)))))
        (show-results results)))))

(defn validate-click-listener
  [elem]
  (events/listen elem "click" validate-handler))

(defn new-step-handler
  "Add new lines for the user to fill in"
  [evt]
  (dorun
    (gdom/appendChild (by-id "proof") (str-to-elem rule-div))
    (gdom/appendChild (by-id "proof") (str-to-elem exp-div))))

(defn new-step-listener
  [elem]
  (events/listen elem "click" new-step-handler))

(defn imply-handler
  [evt]
   (gdom/replaceNode (str-to-elem "<span id=\"type-proof\"> ⇒ </span>")
                     (by-id "type-proof")))

(defn imply-click-listener
  [elem]
  (events/listen elem "click" imply-handler)

  (defn equiv-handler
    [evt]
     (gdom/replaceNode (str-to-elem "<span id=\"type-proof\"> ≡ </span>")
                       (by-id "type-proof"))))

(defn equiv-click-listener
  [elem]
  (events/listen elem "click" equiv-handler))

(defn- replace-with-symbols
  "Replaces all symbol-like strings to real symbols"
  [vanilla-str]
  (-> vanilla-str
      (clojure.string/replace "!" "¬")
      (clojure.string/replace "&" "∧")
      (clojure.string/replace "|" "∨")
      (clojure.string/replace "=>" "⇒")
      (clojure.string/replace "==" "≡")))

(defn keystroke-handler
  "Replaces all symbol-like strings in the focused input box to real symbols"
  [evt]
  (let [input-box (gdom/getActiveElement js/document)
        key (aget evt "key")]
    ;; The last four cases handle fast user typing
    (when (contains? #{"!" "&" "|" "=" ">" "1" "7" "\\" "."} key)
      (gselection/setStart input-box 0)
      (gselection/setEnd input-box (count (.-value input-box)))
      (gselection/setText input-box (replace-with-symbols (gselection/getText input-box)))
      (gselection/setStart input-box (count (.-value input-box)))
      (gselection/setEnd input-box (count (.-value input-box))))))

(defn keystroke-listener
  []
  (events/listen (aget js/document "body") "keyup" keystroke-handler))



;; Top-level handler / listener ===================

(defn window-load-handler
  "Top-level load handler"
  []
  (validate-click-listener (by-id "validate"))
  (new-step-listener (by-id "new-step"))
  (imply-click-listener (by-id "imply"))
  (equiv-click-listener (by-id "equiv"))
  (keystroke-listener))

(events/listen js/window "load" window-load-handler)
