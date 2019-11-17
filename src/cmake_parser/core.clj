(ns cmake-parser.core
  (:require [clojure.string :refer [index-of last-index-of]])
  (:import
   [org.antlr.v4.runtime CharStream CharStreams CommonTokenStream]
   [org.antlr.v4.runtime.tree ParseTreeWalker]
   [cmake_parser CMakeLexer CMakeParser CMakeBaseListener]))


(defn expand-argument
  "Expand argument if it contains variable reference.
  You should provide a variable bindings map.
  Ex. arg: 'Hello ${someone}', bindings: 'someone'->'Bob',
  it will expand to 'Hello Bob'."
  [arg bindings]
  (let [pos2 (index-of arg "}")]
    (if (nil? pos2)
      arg
      (let [pos1 (last-index-of arg "${" pos2)]
        (if (nil? pos1)
          (str (subs arg 0 (+ 1 pos2))
               (expand-argument (subs arg (+ 1 pos2)) bindings))
          (expand-argument (str (subs arg 0 pos1)
                                (get bindings (subs arg (+ 2 pos1) pos2) "")
                                (subs arg (+ 1 pos2)))
                           bindings))))))

(defn parse-string
  "Parse cmake script string, return a list of command invocation."
  [string]
  (let [stream (CharStreams/fromString string)
        lexer (CMakeLexer. stream)
        tokens (CommonTokenStream. lexer)
        parser (CMakeParser. tokens)
        tree (.file parser)
        walker (ParseTreeWalker.)
        invocations (atom [])
        args (atom nil) ;; [command arg1 arg2 ...]
        args-stack (atom nil)
        analyser (proxy [CMakeBaseListener] []
                   (enterCommand_invocation [ctx]
                     (reset! args [(-> ctx (.Identifier) (.getText))]) ;; [command]
                     (reset! args-stack []))
                   (exitCommand_invocation [ctx]
                     (reset! invocations (conj @invocations @args)))
                   (exitSingle_argument [ctx]
                     (reset! args (conj @args (-> ctx (.getText)))))
                   (enterCompound_argument [ctx]
                     (reset! args-stack (conj @args-stack @args))
                     (reset! args []))
                   (exitCompound_argument [ctx]
                     (let [tmp (last @args-stack)]
                       (reset! args-stack (pop @args-stack))
                       (reset! args (conj tmp @args)))))]
    (.walk walker analyser tree)
    @invocations))

(defn parse-file
  "Parse cmake script file, return a list of command invocation."
  [pathname]
  (parse-string (slurp pathname)))


