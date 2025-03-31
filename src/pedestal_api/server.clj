(ns pedestal-api.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as i]
            [pedestal-api.database :as database]))

(defonce server (atom nil))

(defn hello-fn [request]
  {:status 200 :body (str "Hello World! " (get-in request [:query-params :name] "stranger"))})

(defn create-task [req]
  (let [uuid (java.util.UUID/randomUUID)
        task (-> req :query-params :task)
        status (-> req :query-params :status)
        store (:store req)]
    (swap! store assoc uuid {:id uuid :task task :status status})
    {:status 200 :body {:msg "Task created successful"
                        :task {:id uuid :task task :status status}}}))

(defn get-tasks [req]
  {:status 200 :body @(:store req)})

(defn delete-task [req]
  (let [store (:store req)
        task-id (-> (-> req :path-params :id)
                    (java.util.UUID/fromString))]
    (swap! store dissoc task-id)
    {:status 200 :body "Task removed"}))

(defn update-task [req])

(defn assoc-store [context]
  (update context :request assoc :store database/store))

(def db-interceptor
  {:name :db-interceptor
   :enter assoc-store})

(def routes (route/expand-routes
              #{["/hello" ;;endpoint
                 :get ;;method
                 hello-fn ;;function that return from request
                 :route-name :hello-world] ;;every route must have a unique name
                ["/task" :post create-task :route-name :create-task]
                ["/tasks" :get get-tasks :route-name :get-tasks]
                ["/task/:id" :delete delete-task :route-name :delete-task]
                ["/task/:id" :patch update-task :route-name :update-task]}))

(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty ;;define the server, can be another
                  ::http/join? false}) ;;prevent to block Clojure thread (?)

(def service-map-with-interceptor
  (-> service-map
      (http/default-interceptors);;need this to satisfy logic underneath
      (update ::http/interceptors conj (i/interceptor db-interceptor)))) ;;add custom interceptor

(defn start-server []
  (reset! server (http/start(http/create-server service-map-with-interceptor))))

(defn stop-server []
  (http/stop @server))

(defn restart-server []
  (stop-server)
  (start-server))

(comment (restart-server))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(comment (test-request :get "/hello?name=Gustavo"))
(comment (test-request :post "/task?task=teste&status=pending"))
(comment (test-request :post "/task?task=Ler&status=pending"))
(comment (test-request :post "/task?task=Correr&status=pending"))

(comment (test-request :get "/tasks"))
(comment (test-request :delete "/task/dfccca43-9103-4fee-92c4-50d28228b448"))