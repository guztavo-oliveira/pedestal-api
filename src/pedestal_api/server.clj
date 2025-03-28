(ns pedestal-api.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(def store (atom {}))

(def server (atom nil))

(defn hello-fn [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "stranger"))})

(defn create-task [req]
  (let [uuid (java.util.UUID/randomUUID)
        task (-> req :query-params :task)
        status (-> req :query-params :status)]
    (swap! store assoc uuid {:id uuid :task task :status status})
    {:status 200 :body {:msg "Task created successful"
                        :task {:id uuid :task task :status status}}}))

(defn get-tasks [_req]
  {:status 200 :body @store})

(def routes (route/expand-routes
              #{["/hello" ;;endpoint
                 :get ;;method
                 hello-fn ;;function that return from request
                 :route-name :hello-world] ;;every route must have a unique name
                ["/task" :post create-task :route-name :create-task]
                ["/tasks" :get get-tasks :route-name :get-tasks]}))

(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty ;;define the server, can be another
                  ::http/join? false}) ;;prevent to block Clojure thread (?)

(defn start-server []
  (reset! server (http/start(http/create-server service-map))))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(clojure.pprint/pprint (test-request :get "/hello?name=Gustavo"))
(clojure.pprint/pprint (test-request :post "/task?task=teste&status=pending"))
(clojure.pprint/pprint (test-request :post "/task?task=Ler&status=pending"))
(clojure.pprint/pprint (test-request :post "/task?task=Correr&status=pending"))

(clojure.pprint/pprint (test-request :get "/tasks"))