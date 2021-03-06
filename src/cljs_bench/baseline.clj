(ns cljs-bench.baseline
  (:refer-clojure :exclude [pmap]))

(def ^:dynamic *bench-results* (atom []))

(defmacro simple-benchmark-data [bindings expr iterations]
  `(let ~bindings
     (let [start# (System/currentTimeMillis)
           ret# (dotimes [_# ~iterations] ~expr)
           end# (System/currentTimeMillis)
           elapsed# (- end# start#)]
       (swap! cljs-bench.baseline/*bench-results* conj {:bindings '~bindings :expr '~expr
                                                        :elapsed-msecs elapsed#
                                                        :iterations ~iterations}))))


(defrecord Foo [bar baz])

(defn ints-seq
  ([n] (ints-seq 0 n))
  ([i n]
     (when (< i n)
       (lazy-seq
        (cons i (ints-seq (inc i) n))))))

(def r (ints-seq 1000000))

(def pmap (into (clojure.lang.PersistentHashMap/EMPTY)
                [[:a 0] [:b 1] [:c 2] [:d 3] [:e 4] [:f 5] [:g 6] [:h 7]
                 [:i 8] [:j 9] [:k 10] [:l 11] [:m 12] [:n 13] [:o 14] [:p 15]
                 [:q 16] [:r 17] [:s 18] [:t 19] [:u 20] [:v 21] [:w 22] [:x 23]
                 [:y 24] [:z 25] [:a0 26] [:b0 27] [:c0 28] [:d0 29] [:e0 30] [:f0 31]]))

(defn baseline-benchmark []
  (binding [*bench-results* (atom [])]
    (simple-benchmark-data [coll (list 1 2 3)] (first coll) 1000000)
    (simple-benchmark-data [coll (list 1 2 3)] (rest coll) 1000000)
    (simple-benchmark-data [] (list) 1000000)
    (simple-benchmark-data [] (list 1 2 3) 1000000)
    
    (simple-benchmark-data [] [] 1000000)
    (simple-benchmark-data [] [1 2 3] 1000000)
    (simple-benchmark-data [coll [1 2 3]] (transient coll) 100000)
    (simple-benchmark-data [coll [1 2 3]] (nth coll 0) 1000000)
    (simple-benchmark-data [coll [1 2 3]] (conj coll 4) 1000000)
    (simple-benchmark-data [coll [1 2 3]] (seq coll) 1000000)
    (simple-benchmark-data [coll (seq [1 2 3])] (first coll) 1000000)
    (simple-benchmark-data [coll (seq [1 2 3])] (rest coll) 1000000)
    (simple-benchmark-data [coll (seq [1 2 3])] (next coll) 1000000)
    
    (simple-benchmark-data [coll (take 100000 (iterate inc 0))] (reduce + 0 coll) 1)
    (simple-benchmark-data [coll (range 1000000)] (reduce + 0 coll) 1)
    (simple-benchmark-data [coll (into [] (range 1000000))] (reduce + 0 coll) 1)
    
    (simple-benchmark-data [coll {:foo 1 :bar 2}] (get coll :foo) 1000000)
    (simple-benchmark-data [coll {:foo 1 :bar 2}] (:foo coll) 1000000)
    
    (simple-benchmark-data [coll (Foo. 1 2)] (:bar coll) 1000000)
    (simple-benchmark-data [coll {:foo 1 :bar 2}] (assoc coll :baz 3) 100000)
    (simple-benchmark-data [coll {:foo 1 :bar 2}] (assoc coll :foo 2) 100000)

    (simple-benchmark-data [key :f0] (hash key) 100000)
    (simple-benchmark-data [coll {:foo 1 :bar 2}]
      (loop [i 0 m coll]
        (if (< i 100000)
          (recur (inc i) (assoc m :foo 2))
          m))
      1)
    
    (simple-benchmark-data [coll pmap] (:f0 coll) 1000000)
    (simple-benchmark-data [coll pmap] (get coll :f0) 1000000)
    (simple-benchmark-data [coll pmap] (assoc coll :g0 32) 1000000)
    (simple-benchmark-data [coll clojure.lang.PersistentHashMap/EMPTY] (assoc coll :f0 1) 1000000)
    
    (simple-benchmark-data [coll (range 500000)] (reduce + coll) 1)
    
    (simple-benchmark-data [s "{:foo [1 2 3]}"] (read-string s) 1000)
    
    (simple-benchmark-data [r (range 1000000)] (last r) 1)
    
    (simple-benchmark-data [r r] (last r) 1)
    (simple-benchmark-data [r r] (last r) 1)
    
    @*bench-results*
    
    ))

