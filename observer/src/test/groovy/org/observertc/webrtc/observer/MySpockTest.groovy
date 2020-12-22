/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer


import spock.lang.Specification
import spock.lang.Unroll

public class MySpockTest extends Specification {

//    MathService mathService
    Service mathService = new Service();

    public class Service {
        public int compute(int num) {
            return num * 4;
        }
    }

    @Unroll
    void "should compute #num times 4"() {
        when:
        def result = mathService.compute(num)

        then:
        result == expected

        where:
        num | expected
        2   | 8
        3   | 12
    }
}