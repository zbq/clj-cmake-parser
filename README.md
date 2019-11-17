# clj-cmake-parser

This is a project to parse cmake scripts, it will parse script into a list of command invocation.

## API

1. expand-argument [arg bindings]
     Expand argument if it contains variable reference.
     You should provide a variable bindings map.
     Ex. arg: 'Hello ${someone}', bindings: 'someone'->'Bob',
     it will expand to 'Hello Bob'.

2. parse-string [string]
     Parse cmake script string, return a list of command invocation.

3. parse-file [pathname]
     Parse cmake script file, return a list of command invocation.

## Example
```
(defn common-usage []
  (let [bindings (atom {})
        invocations (parse-string "# A demo helloworld
                                   SET(TARGET_NAME \"HelloWorld\")
                                   ADD_EXECUTABLE(${TARGET_NAME} hello.cpp world.cpp)")]
    (doseq [invocation invocations]
      (println (clojure.string/join " " invocation))
      (cond
        (.equalsIgnoreCase "SET" (first invocation))
        (reset! bindings (assoc @bindings (second invocation) (nth invocation 2)))
        (.equalsIgnoreCase "ADD_EXECUTABLE" (first invocation))
        (println "bin:" (expand-argument (second invocation) @bindings))))))
```
This will output: 

    - SET TARGET_NAME "HelloWorld"
    - ADD_EXECUTABLE ${TARGET_NAME} hello.cpp world.cpp 
    - bin: "HelloWorld"

## CMake language grammar

https://cmake.org/cmake/help/v3.12/manual/cmake-language.7.html

## Repository

https://github.com/zbq/clj-cmake-parser.git

## License

Eclipse Public License

