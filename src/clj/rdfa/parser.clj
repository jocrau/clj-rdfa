(ns rdfa.parser)

(defprotocol Parser
  (get-rdfa [source] [source options]))
