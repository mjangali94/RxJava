/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava3.internal.functions;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import org.junit.rules.TestName;
import org.junit.Test;

import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObjectHelperTest extends RxJavaTest {

    @org.junit.Rule public TestName name = new TestName();
    @org.junit.Before
    public void myBefore() throws IOException {
    	FileWriter fw = new FileWriter("/Users/massi/Desktop/tmp.csv", true);
    	fw.write(this.getClass().getName()+"."+name.getMethodName() +","+io.reactivex.rxjava3.core.myBlackhole.hitting_count()+"\n");
    	fw.close();
    }
@Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObjectHelper.class);
    }

    @Test
    public void verifyPositiveInt() throws Exception {
        assertEquals(1, ObjectHelper.verifyPositive(1, "param"));
    }

    @Test
    public void verifyPositiveLong() throws Exception {
        assertEquals(1L, ObjectHelper.verifyPositive(1L, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveIntFail() throws Exception {
        assertEquals(-1, ObjectHelper.verifyPositive(-1, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveLongFail() throws Exception {
        assertEquals(-1L, ObjectHelper.verifyPositive(-1L, "param"));
    }
}
