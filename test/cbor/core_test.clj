;   Copyright (c) 2015 Erwin Kroon, Nico Rikken. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns cbor.core-test
  (:require [clojure.test :refer :all]
            [cbor.core :as sut]))

;;TODO: add rest of rfc examples
(def rfc-examples
  [0                     0x00
   1                     0x01
   10                    0x0a
   23                    0x17
   24                    0x1818
   25                    0x1819
   100                   0x1864
   1000                  0x1903e8
   1000000               0x1a000f4240
   1000000000000         0x1b000000e8d4a51000 ;;10
   18446744073709551615  0x1bffffffffffffffff
   18446744073709551616  0xc249010000000000000000
   -18446744073709551616 0x3bffffffffffffffff
   -18446744073709551617 0xc349010000000000000000
   -1                    0x20
   -10                   0x29
   -100                  0x3863
   -1000                 0x3903e7])

(defn compare-sequences [& s]
  (and (apply = (map count s))
       (every? true? (apply map = s))))

(defn- test-pair [[first second]]
  (let [decoded-in first
        encoded-in (-> second biginteger (.toByteArray))
        encoded    (sut/encode decoded-in)
        decoded    (sut/decode encoded-in)]
    (testing (str "encoding expected: " (vec encoded-in) ", actual: " (vec encoded))
      (is (= true (compare-sequences encoded-in encoded))))
    (testing (str "decoding expected: " decoded-in ", actual: " decoded)
      (is (= decoded-in decoded)))))

;;TODO: take more than 4
(deftest rfc-test []
  (testing "rfc input"
    (let [pairs (take 10 (partition 2 rfc-examples))]
      (doall (map test-pair pairs)))))
