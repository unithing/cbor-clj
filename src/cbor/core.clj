;   Copyright (c) 2015 Erwin Kroon, Nico Rikken. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns cbor.core)

(defn- zeros* [n]
  (byte-array (repeat n 0x00)))

(def zeros (memoize zeros*))

(defn- make-n-bytes [bytes n]
  (let [actual-n (count bytes)]
    (if (< actual-n n)
      (byte-array (concat (zeros (- n actual-n)) bytes))
      (byte-array (take-last n bytes)))))

(defn- encode-uint [prefix nr value]
  (into (vector)
        (concat [prefix]
                (-> (biginteger value)
                    (.toByteArray)
                    (make-n-bytes nr)))))

(defn encode [value]
  (condp >= value
    0x17               (byte-array (vector value))
    0xff               (byte-array (encode-uint 0x18 1 value))
    0xffff             (byte-array (encode-uint 0x19 2 value))
    0xffffffff         (byte-array (encode-uint 0x1a 4 value))
    0xffffffffffffffff (byte-array (encode-uint 0x1b 8 value))))

(defn- shifted-byte [n b]
  (bit-shift-left (bit-and 0xff (int b)) (* n 8)))

(defn- parse-bytes [bs]
  (let [idxbs (map-indexed (fn [a b] [a b]) bs)
        shifted-bytes (map (fn [[a b]] (shifted-byte a b)) idxbs)]
    (reduce bit-or shifted-bytes)))

(defn- take-bytes [n bytes]
  (reverse (take n (rest bytes))))

(defn decode [bytes]
  (cond
    (>= 0x17 (first bytes)) (first bytes)
    (= 0x18  (first bytes)) (->> bytes (take-bytes 1) parse-bytes)
    (= 0x19  (first bytes)) (->> bytes (take-bytes 2) parse-bytes)
    (= 0x1a  (first bytes)) (->> bytes (take-bytes 4) parse-bytes)
    (= 0x1b  (first bytes)) (->> bytes (take-bytes 8) parse-bytes)) )
