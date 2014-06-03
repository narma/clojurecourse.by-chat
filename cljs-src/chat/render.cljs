(ns chat.render
  (:require [kioo.reagent :refer [add-class set-attr set-style
                                  append content substitute listen do->
                                  html-content remove-class]]
            [reagent.core :as reagent :refer [atom]]
            [goog.async.AnimationDelay]
            [chat.service :as service :refer [messages]])
  (:require-macros [kioo.reagent :refer [defsnippet deftemplate]]))
null

(def tmp (atom nil))

(defsnippet Header "templates/index.html" [:div#header]
  [group]
  {[:.group_name] (do->
                   (content (str group)))})


(deftemplate Channel "templates/channel.html"
  [ch-name]
  {[:.channel_name] (content ch-name)})


(deftemplate Message "templates/msg-full.html"
  [message full]
  {[:.message] (if full (add-class "show_user" "first" "divider")
                 (remove-class "show_user"))
   [:.message_content] (content (:msg message))
   [:.member_preview_link] (if full (set-style
                                     :background-image
                                     (str "url('" (get-in message [:user :avatar-url])
                                          "')"))
                             (substitute (html-content "")))
   [:.message_sender] (if full (do->
                                (set-attr :data-memeber-id (get-in message [:user :dbid]))
                                (content (get-in message [:user :name])))
                        (substitute (html-content "")))

   [:.timestamp] (content (let [h (.getHours (:created message))
                                m (.getMinutes (:created message))]
                            (str h":"m)))
   })

(def messages-height (atom 0))
(def window (js/$ js/window))


(defn full-message? [msg prev]
  (or (nil? prev)
      (not= (:user msg) (:user prev))))

(defn get-key-code [react-event]
  (.-keyCode (.-nativeEvent react-event)))

(deftemplate App "templates/index.html" []
  {[:#user_avatar] (set-attr "src" (aget js/user_info "avatar-url"))
   [:#user_name] (content (aget js/user_info "name"))
   [:#messages_wrap] (set-style "height" (str @messages-height "px"))
   [:#header] (substitute (Header "Global chat"))
   [:#channel-list] (append (Channel "general"))
   [:#message-input] (listen :onKeyDown #(when (= 13 (get-key-code %))
                                           (.preventDefault %)
                                           (let [el (js/$ (.-target %))
                                                 msg (.val el)]
                                             (service/msg-send msg)
                                             (.val el ""))))
   [:#messages_rows] (apply do->
                            (for [[msg prev] (zipmap @messages
                                                     (concat [nil] @messages))
                                  :let [full? (full-message? msg prev)]]
                              (append (Message msg full?))))
   })

(defn main [conn]
  (js/$ (fn [$]
          (.resize window #(let [height (- (.height window) (+ 53 64))]
                               (reset! messages-height height)))
          (.resize window)
            ))
  (reagent/render-component [App] (.-body js/document))
  (service/load-history))

