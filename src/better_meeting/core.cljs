(ns better-meeting.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(def meeting-data (r/atom {:attendees 2
                           :duration 15
                           :context 20
                           :meeting 30
                           :business 0.1}))

(defn meeting-time [attendees duration]
  (* attendees duration))

(defn context-switching-time [attendees]
  (let [recovery-time 10] ; https://erichorvitz.com/CHI_2007_Iqbal_Horvitz.pdf
    (* attendees recovery-time)))

(defn meeting-time-cost
  [attendees duration]
  (let [meeting (meeting-time attendees duration)
        context-switch (context-switching-time attendees)]
    (/ (+ meeting context-switch) 480) ; 60 minutes in one hour, 8 hours in one business day
    ))

(defn calc
  [attendees duration]
  (let [meeting (meeting-time attendees duration)
        context (context-switching-time attendees)
        business (meeting-time-cost attendees duration)]
    {:attendees attendees
     :duration duration
     :context context
     :meeting meeting
     :business business}))

(comment
  (meeting-time 7 60)
  (context-switching-time 10)
  (meeting-time-cost 7 60))


(defn attendees-input [value]
  [:input {:type "number" :default-value value  :min 1
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [duration]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (calc new-value duration)))))}])

(defn duration-input [value]
  [:input {:type "number" :default-value value :min 0 :step 15
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [attendees]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (calc attendees new-value)))))}])

(defn number->currency [number]
  (.toLocaleString number (.-language js/navigator) #js {:style "currency" :currency "USD"}))

;; -------------------------
;; Views

(defn home-page []
  (let [{:keys [attendees duration context meeting business]} @meeting-data]
    [:div
     [:div "attendees "
      [:br]
      [attendees-input attendees]]
     [:div "duration (minutes) "
      [:br]
      [duration-input duration]]
     [:div "meeting time: " meeting " minutes"]
     [:div [:a {:href "https://erichorvitz.com/CHI_2007_Iqbal_Horvitz.pdf"} "context switching"] " tax: " context " minutes"]
     [:div [:h2 "total: " business]] " business days"]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
