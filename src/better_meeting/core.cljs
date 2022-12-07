(ns better-meeting.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(def meeting-data (r/atom {:attendees 2 :duration 15 :salary 30000 :cost 12.01}))

(defn meeting-cost [attendees duration salary]
  (let [salary-per-minute (/ salary 52 40 60) ; 52 weeks in one year, 40 hours in one work week, 60 minutes in one hour
        cost (* attendees duration salary-per-minute)]
    {:attendees attendees :duration duration :salary salary :cost cost}))

(defn attendees-input [value]
  [:input {:type "number" :default-value value  :min 1
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [duration salary]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (meeting-cost new-value duration salary)))))}])

(defn duration-input [value]
  [:input {:type "number" :default-value value :min 0 :step 15
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [attendees salary]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (meeting-cost attendees new-value salary)))))}])

(defn salary-input [value]
  [:input {:type "number" :default-value value :min 0 :step 5000
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [attendees duration]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (meeting-cost attendees duration new-value)))))}])

(defn number->currency [number]
  (.toLocaleString number (.-language js/navigator) #js {:style "currency" :currency "USD"}))

;; -------------------------
;; Views

(defn home-page []
  (let [{:keys [attendees duration salary cost]} @meeting-data]
    [:div
     [:div "meeting cost:"]
     [:h2 (number->currency cost)]
     [:div "attendees "
      [:br]
      [attendees-input attendees]]
     [:div "duration (minutes) "
      [:br]
      [duration-input duration]]
     [:div "average salary (yearly) "
      [:br]
      [salary-input salary]]]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
