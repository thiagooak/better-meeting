(ns better-meeting.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

(def meeting-data (r/atom {:attendees 2
                           :duration 15
                           :context 20
                           :meeting 30
                           :business 0.1
                           :note ""}))

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

(defn round [number precision]
  (.toLocaleString number (.-language js/navigator) #js {:maximumFractionDigits precision}))

(defn total->note [total]
  (let [t (round total 0)
        ; these are wrong as they consider 24h days and out unit is 8h days (business days)
        map {"3" "or the amount of time it would take you to watch every episode of Breaking Bad"
             "6" "or the amount of time it would take you to watch the original Star Ward trilogy"
             "9" "or the amount of time it would take you to watch every episode of RuPaul's Drag Race"
             }]
    (map t)
    )
  )

(defn calc
  [attendees duration]
  (let [meeting (meeting-time attendees duration)
        context (context-switching-time attendees)
        business (meeting-time-cost attendees duration)
        note (total->note business)]
    {:attendees attendees
     :duration duration
     :context context
     :meeting meeting
     :business business
     :note note}))

(defn attendees-input [value]
  [:input {:type "number"
           :class "w-16 border-2 border-indigo-500 p-2"
           :default-value value  :min 1
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [duration]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (calc new-value duration)))))}])

(defn duration-input [value]
  [:input {:type "number"
           :class "w-24 border-2 border-indigo-500 p-2"
           :default-value value :min 0 :step 15
           :on-change (fn [e]
                        (let [new-value (js/parseInt (.. e -target -value))
                              {:keys [attendees]} @meeting-data]
                          (swap! meeting-data
                                 (fn []
                                   (calc attendees new-value)))))}])

;; -------------------------
;; Views

(defn home-page []
  (let [{:keys [attendees duration context meeting business note]} @meeting-data]
    [:div {:class "mx-auto max-w-xl font-mono my-4"}
     [:h1 {:class "text-xl mb-4"} "How much time goes into a meeting?"]
     [:div "A " [duration-input duration] " minute meeting with "
      [attendees-input attendees] " people takes:"]
     [:div {:class "border-dashed border-t-2 border-gray-600 my-4"}]
     [:div {:class "grid grid-cols-2 gap-2 pl-8"}
      [:div "+ combined meeting time"]
      [:div meeting [:span {:class "text-sm"} " minutes"]]
      [:div "+ " [:a {:class "underline" :href "https://erichorvitz.com/CHI_2007_Iqbal_Horvitz.pdf"} "context switch"] " tax "]
      [:div context [:span {:class "text-sm"} " minutes"]]
      ]
     [:div {:class "border-dashed border-t-2 border-gray-600 my-4"}]
     [:div {:class "grid grid-cols-2 gap-2 pl-8"}
      [:div "= total time investment"]
      [:div {:class "text-indigo-500 font-bold"} (round business 1) [:span {:class "text-sm"}" business days"]]
      [:div " "]
[:div {:class "text-gray-400"} note]
      ]
     [:div {:class "text-gray-300 mt-12"}
      [:a { :href "https://github.com/thiagooak/better-meeting" :target "_blank"} [:svg {:xmlns "http://www.w3.org/2000/svg" :fill "none" :view-box "0 0 24 24" :stroke-width "1.5" :stroke "currentColor" :class "w-6 h-6"}
                                                                                                                    [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M17.25 6.75L22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3l-4.5 16.5"}]]]]
     ]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
