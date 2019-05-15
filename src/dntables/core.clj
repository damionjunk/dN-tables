(ns dntables.core
  (:require [clj-pdf.core :as pdf]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [dntables.parsers.text :as tp]
            [dntables.runner :as run])
  (:gen-class))
(def cli-options
  [
   ["-t" "--latex" "LaTeX output file mode"]
   ["-p" "--pdf" "PDF output file mode"]
   ["-j" "--json" "JSON output file mode"]
   ["-e" "--edn" "END output file mode"]
   ["-h" "--help"]
   ])

(defn usage [options-summary]
  (->> ["dN Tables: Printable tables from text files."
        ""
        "Usage: program-name [options] input-file output-file"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the GitHub project page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 2 (count arguments)))
      {:in (first arguments) :out (second arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [in out options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do
        (println "Input file: " in)
        (println "Output file: " out)
        (println "Options:" options)
        (run/dispatch in out options)
        (println "Done.")))))