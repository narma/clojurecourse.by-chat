(ns me.narma.auth.protocols)

(defprotocol UserAuthBackend
  ""
  (authenticate [_]
     "Must try to authenticate user and return a valid ring response")
  (knock [_]
     "Handle request from OAuth providers")
  (user-info [_]
     "Must return a hashmap with keys: name (mandatory), avatar-url (optional)"))
