(ns compound.indexes.multi
  (:require [compound.core :as c]
            [clojure.spec.alpha :as s]))

(defmethod c/index-def-spec :compound.index.types/multi
  [_]
  (s/keys :req [:compound.index/key-fn :compound.index/id :compound.index/type]))

(defmethod c/index-def->behaviour :compound.index.types/multi
  [index-def]
  (let [{:compound.index/keys [key-fn]} index-def]
    {:compound.index.behaviour/empty {}
     :compound.index.behaviour/add (fn [index added]
                                     (let [new-index (reduce (fn add-items [index item]
                                                               (let [k (key-fn item)
                                                                     existing-items (get index k #{})]

                                                                 (assoc! index k (conj existing-items item))))
                                                             (transient index)
                                                             added)]
                                       (persistent! new-index)))
     :compound.index.behaviour/remove (fn [index removed]
                                        (let [new-index (reduce (fn remove-items [index item]
                                                                  (let [k (key-fn item)
                                                                        existing-items (get index k #{})]
                                                                    (assoc! index k (disj existing-items item))))
                                                                (transient index)
                                                                removed)]
                                          (persistent! new-index)))}))