//        Copyright (C) 2016 DigiGene, (www.DigiGene.com)(alinhayati[at]gmail[dot]com)
//
//        Licensed under the Apache License, Version 2.0 (the "License");
//        you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software
//        distributed under the License is distributed on an "AS IS" BASIS,
//        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//        See the License for the specific language governing permissions and
//        limitations under the License.

package com.digigene.autoupdate;

public class Utils {
    public static void checkArguments(String[] fields) {
        for (String field : fields) {
            Utils.throwExceptionIfEmpty(field);
        }
    }

    public static void throwExceptionIfEmpty(String field) {
        if (field.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " in JsonKeys must not be null");
        }
    }
}
