(ns edsger.core
  "Front-end UI controller"
  (:require [clojure.browser.repl :as repl]
            [clojure.browser.dom  :as dom]
            [goog.events :as events]
            [goog.dom :as gdom]
            [edsger.unification :as uni]
            [edsger.parsing :as parsing]))

(enable-console-print!)



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



;; UI and handlers ===================

(defn copy-handler-gen
  "Returns a function/handler that copies the given element's content
   to the clipboard"
  [id]
  (fn []
    (let [temp (elemt "input" {"value" (aget (by-id id) "innerHTML")})]
      (do
        (gdom/appendChild (aget js/document "body") temp)
        (.select temp)
        (.execCommand js/document "copy")
        (gdom/removeNode temp)))))

(defn copy-click-listener
  [elem id]
  (events/listen elem "click" (copy-handler-gen id)))

;; Bootstrap alert div
(def parse-err-str
  (str "<div class=\"alert alert-danger alert-dismissible fade show col-2.5\" role=\"alert\">"
         "<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\">"
           "<span aria-hidden=\"true\">&times;</span>"
         "</button>"
         "Error in input"
       "</div>"))

(defn- merge-val
  [curr-map val]
  (let [curr-map (update curr-map :curr-id inc)
        curr-id (:curr-id curr-map)]
    (if (nil? val) (update-in curr-map [:locations] #(conj % curr-id)) curr-map)))

(defn- show-exp-parse-err
  [err-id-vec]
  (let [exp-boxes (iArrayLike-to-cljs-list (gdom/getElementsByClass "exp-box"))]
    (dorun
     (map
      (fn [id]
        ;; adding err msg where they fail
        (gdom/appendChild (nth exp-boxes id) (str-to-elem parse-err-str)))
      err-id-vec))))

(defn- show-rule-parse-err
  [err-id-vec]
  (let [rule-cols (map #(gdom/getParentElement %)
                       (iArrayLike-to-cljs-list (gdom/getElementsByClass "rule")))]
    (dorun
     (map
      (fn [id]
        ;; adding err msg where they fail
        (gdom/insertSiblingAfter (str-to-elem parse-err-str) (nth rule-cols id)))
      err-id-vec))))

(defn validate-handler
  "Performs the validation based on the values typed by users"
  [evt]
  (let [exp-str-li (map #(.-value %) (iArrayLike-to-cljs-list (gdom/getElementsByClass "ex")))
        rule-str-li (map #(.-value %) (iArrayLike-to-cljs-list (gdom/getElementsByClass "rule")))
        non-empty-input (every? #(not= "" %) (concat exp-str-li rule-str-li))
        exps (map #(parsing/parse %) exp-str-li)
        vanilla-rules (map #(parsing/parse %) rule-str-li)
        rules (map #(parsing/rulify %) vanilla-rules)
        ;; vector of indices where parsing err occurred (e.g., [0, 3])
        exp-parse-err (:locations (reduce merge-val {:curr-id -1 :locations []} exps))
        rule-parse-err (:locations (reduce merge-val {:curr-id -1 :locations []} vanilla-rules))
        result-str (str (true? (uni/check-match-recursive (nth exps 0)
                                                          (nth exps 1)
                                                          (nth rules 0)
                                                          (nth rules 1))))]
    (when non-empty-input
      (cond
        (not (empty? exp-parse-err)) (show-exp-parse-err exp-parse-err)
        (not (empty? rule-parse-err)) (show-rule-parse-err rule-parse-err)
        :else (.alert js/window result-str)))))

(defn validate-click-listener
  [elem]
  (events/listen elem "click" validate-handler))



;; Top-level handler / listener ===================

(defn window-load-handler
  "Top-level load handler"
  []
  (validate-click-listener (by-id "validate"))
  (dorun
   (map #(copy-click-listener (by-id %) %)
        ["not" "and" "or" "impli" "equiv"])))

(events/listen js/window "load" window-load-handler)
