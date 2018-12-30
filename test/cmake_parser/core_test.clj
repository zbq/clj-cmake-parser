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

(deftest test-crlf
  (let [script "add(1 2)\r\nsub(2 3) #hello"
        invocs (parse-string script)]
    (is (= (count invocs) 2))
    (is (= (count (first invocs)) 3))
    (is (= (first (first invocs)) "add"))
    (is (= (second (first invocs)) "1"))
    (is (= (nth (first invocs) 2) "2"))))

(deftest test-compound-argument
  (let [script "
if(FALSE AND (FALSE OR (HELLO WORLD) TRUE)) # evaluates to FALSE
"
        invocs (parse-string script)
        invoc (first invocs)]
    (is (= (count invoc) 4))
    (is (= (count (nth invoc 3)) 4))
    (is (= "if FALSE AND FALSE OR HELLO WORLD TRUE"
           (clojure.string/join " " (flatten invoc))))))

(deftest test-bracket-comment
  (let [script "
message(\"First Argument\\n\" #[[Bracket Comment]] \"Second Argument\")"
        invocs (parse-string script)
        invoc (first invocs)]
    (is (= (count invoc) 3))
    (is (= (last invoc) "\"Second Argument\""))))

(deftest test-line-comment
  (let [script "
message(\"First Argument\\n\" # This is a line comment :)
        \"Second Argument\") # This is a line comment."
        invocs (parse-string script)
        invoc (first invocs)]
    (is (= (count invoc) 3))
    (is (= (last invoc) "\"Second Argument\""))))

(deftest test-expand-arg
  (let [bindings (transient {})]
    (is (= (expand-argument "abc" bindings) "abc"))
    (is (= (expand-argument "a${b}c" bindings) "ac"))
    (assoc! bindings "b" "B")
    (is (= (expand-argument "a${b}c" bindings) "aBc"))
    ;(is (= (expand-argument "a${B}c" bindings) "aBc"))
    (assoc! bindings "xBx" 1024)
    (is (= (expand-argument "a${x${b}x}c" bindings) "a1024c"))
    (is (= (expand-argument "a${b${xBx}c" bindings) "a${b1024c"))
    (is (= (expand-argument "a{}b${b}c{}d" bindings) "a{}bBc{}d"))
    ))
