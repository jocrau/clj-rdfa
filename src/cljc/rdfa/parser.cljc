(ns rdfa.parser)

(defprotocol Parser
  (parse [source] [source options]))
