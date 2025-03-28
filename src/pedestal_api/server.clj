(ns pedestal-api.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(defn hello-fn [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "stranger"))})

(def routes (route/expand-routes #{["/hello" ;;endpoint
                                    :get ;;method
                                    hello-fn ;;function that return from request
                                    :route-name :hello-world]})) ;;every route must have a unique name

(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty ;;define the server, can be another
                  ::http/join? false}) ;;prevent to block Clojure thread (?)

(def server (atom nil))

(defn start-server []
  (reset! server (http/start(http/create-server service-map))))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(clojure.pprint/pprint (test-request :get "/hello?name=Gustavo"))

