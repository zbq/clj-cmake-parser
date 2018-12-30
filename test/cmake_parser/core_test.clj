(ns cmake-parser.core-test
  (:require [clojure.test :refer :all]
            [clojure.string]
            [cmake-parser.core :refer :all]))

(defn common-usage []
  (let [bindings (transient {})
        invocations (parse-string "# A demo helloworld
                                   SET(TARGET_NAME \"HelloWorld\")
                                   ADD_EXECUTABLE(${TARGET_NAME} hello.cpp world.cpp)")]
    (doseq [invocation invocations]
      (println (clojure.string/join " " invocation))
      (cond
        (.equalsIgnoreCase "SET" (first invocation))
        (assoc! bindings (second invocation) (nth invocation 2))
        (.equalsIgnoreCase "ADD_EXECUTABLE" (first invocation))
        (println "bin:" (expand-argument (second invocation) bindings))))))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
