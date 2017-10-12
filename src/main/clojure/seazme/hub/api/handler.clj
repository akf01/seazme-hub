(ns seazme.hub.api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clj-uuid :as uuid]
            [ring.logger :as logger]
            ))

(s/defschema Total {:id String (s/optional-key :total) Long :comment String})
(s/defschema UpdateRange {:from Long :to Long})


;;TODO define , :id #"[0-9]+"

(def wip "wip: API response may not be fully defined yet")

(defn authorized-for-docs? [handler]
  (fn [request]
    (let [auth-header (get (:headers request) "authorization")]
      (cond
        (nil? auth-header)
        (-> (unauthorized)
            (header "WWW-Authenticate" "Basic realm=\"whatever\""))

        (= auth-header "Basic .*")
        (handler request)

        :else
        (unauthorized {})))))

(def handler
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Octogora API"
                   :description "please see docs/DESIGN.org for details"}
            :tags [{:name :access :description "application provisioning: registration/status/deactivation"}
                   {:name :intake :description "data intake process"}
                   {:name :data-read :description "raw and processed data reading"}
                   {:name :analytics :description "analytics results access"}
                   {:name :system :description "system status, stats and meta info"}]
            :securityDefinitions {:BasicAuth
                                  {:type "basic"}}}}}

   (context "/v1" []
            (context "/access" []
                     :tags [:access]
                     (GET "/apikey" []
                          :return {:Authorization String :comment String}
                          :summary "This is experimental: generates API basic auth (or other) key after completing SSO (PP, Google, FB, etc) based authetication. Otherwise, apikey are provisioned manually."
                          (ok {:Authorization (str (uuid/v4)) :comment wip}))
                     )
            )
   (context "/v1" []
            :middleware [authorized-for-docs?]
            (context "/access" []
                     :tags [:access]
                     (context "/intake" []
                              :tags [:access :intake]
                              (PUT "/applications" []
                                   :return {:id String :comment String}
                                   :query-params [description :- String, source_bussines_unit :- String, source_type :- String, source_instance :- String, email :- String]
                                   :summary "intake application registration async request"
                                   (ok {:id (str (uuid/v4)) :comment wip}))
                              )
                     (context "/data-read" []
                              :tags [:access :data-read]
                              (PUT "/applications" []
                                   :return {:comment String}
                                   :query-params [description :- String, email :- String]
                                   :summary "data-read application registration async request"
                                   (ok {:comment wip}))
                              )
                     (context "/analytics" []
                              :tags [:access :analytics]
                              (PUT "/applications" []
                                   :return {:comment String}
                                   :query-params [description :- String, email :- String]
                                   :summary "analytics application registration async request"
                                   (ok {:comment wip}))
                              )
                     (GET "/applications/:id" [id]
                          :return {:request_status String :expires Long :comment String}
                          :summary "application registration status"
                          (ok {:request_status "accepted" :expires 0 :comment wip}))
                     (GET "/applications" []
                          :return {:ids [String] :comment String}
                          :summary "available applications"
                          (ok {:ids [] :comment wip}))
                     (PUT "/applications/:id/deactivate" [id]
                          :return {:comment String}
                          :summary "application deactivation"
                          (ok {:comment wip}))
                     )
            (context "/system" []
                     :tags [:system]
                     (GET "/status" []
                          :return {:comment String s/Keyword s/Any}
                          :summary "current system status report"
                          (ok {:comment wip})
                          )
                     )
            (context "/intake" []
                     :tags [:intake]
                     (PUT "/sessions" []
                          :return {:id String :expires Long :comment String (s/optional-key :update-range) UpdateRange}
                          :query-params [description :- String, incremental :- Boolean]
                          :summary "document intake session begin, like SQL's \"BEGIN TRANSACTION\""
                          (ok {:id (str (uuid/v4)) :expires 0 :comment wip}))
                     (POST "/sessions/:id/document" [id]
                           :body [json String]
                           :return {:comment String}
                           :summary "document (e.g. page in Confluence) or any other atomic piece of data store"
                           (ok {:comment wip}))
                     (PUT "/sessions/:id/submit" [id]
                           :return {:comment String}
                           :summary "sucesfull session end declaration, like SQL's \"COMMIT TRANSACTION\""
                           (ok {:comment wip}))
                     (PUT "/sessions/:id/cancel" [id]
                           :return {:comment String}
                           :summary "session cancellation declaration, like SQL's \"ROLLBACK TRANSACTION\""
                           (ok {:comment wip}))
                     (GET "/sessions/:id/cancel" [id]
                          :return {:comment String}
                          :summary "session status"
                          (ok {:comment wip}))
                     )
            (context "/data-read" []
                     :tags [:data-read]
                     (GET "/dummy" []
                          :return {:comment String}
                          :summary "To be developed. (dummy get exists in order to show up this context in swagger ui.)"
                          (ok {:comment wip}))
                     )

            (context "/analytics" []
                     :tags [:analytics]
                     (GET "/dummy" []
                          :return {:comment String}
                          :summary "To be developed. (dummy get exists in order to show up this context in swagger ui.)"
                          (ok {:comment wip}))
                     )
            )
   )
  )

(def app (-> handler logger/wrap-with-logger))
