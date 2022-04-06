/*
 * Copyright 2013-2014 Grzegorz Ligas <ligasgr@gmail.com> and other contributors
 * (see the CONTRIBUTORS file).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.xquery.runner.rt;

import java.io.IOException;
import java.io.OutputStream;

/**
* User: ligasgr
* Date: 09/01/14
* Time: 17:33
*/
public class StringOutputStream extends OutputStream {
    StringBuilder buffer = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        buffer.append((char) b);
    }

    public String getString() {
        return buffer.toString().replaceAll("\n","");
    }
}
