/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
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

package org.symphonyoss.integration.core.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link LocalAuthorizationRepoServiceCondition}
 * Created by hamitay on 18/08/17.
 */
public class LocalAuthorizationRepoServiceConditionTest {

  private static final String PROPERTY_KEY = "local_authorization";

  @Test
  public void testLocalAuthorizationRepoService() {
    LocalAuthorizationRepoServiceCondition condition = new LocalAuthorizationRepoServiceCondition();

    System.getProperties().remove(PROPERTY_KEY);
    assertFalse(condition.matches(null, null));

    System.setProperty(PROPERTY_KEY, "false");
    assertFalse(condition.matches(null, null));

    System.setProperty(PROPERTY_KEY, "true");
    assertTrue(condition.matches(null, null));
  }

}
