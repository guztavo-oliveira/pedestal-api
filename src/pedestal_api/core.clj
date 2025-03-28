(ns pedestal-api.core
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]))

(defn hello-fn [request]
  {:status 200 :body "Hello World"})

(def routes (route/expand-routes #{["/hello" ;;endpoint
                                    :get ;;method
                                    hello-fn ;;function that return from request
                                    :route-name :hello-world]})) ;;every route must have a unique name

(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty ;;define the server, can be another
                  ::http/join? false}) ;;prevent to block Clojure thread (?)

(http/start (http/create-server service-map))
(prn "Server running")
