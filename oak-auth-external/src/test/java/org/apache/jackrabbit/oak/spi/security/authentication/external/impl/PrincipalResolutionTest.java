/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.spi.security.authentication.external.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentity;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityException;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityProvider;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalIdentityRef;
import org.apache.jackrabbit.oak.spi.security.authentication.external.ExternalUser;
import org.apache.jackrabbit.oak.spi.security.authentication.external.PrincipalNameResolver;
import org.apache.jackrabbit.oak.spi.security.authentication.external.SyncResult;
import org.apache.jackrabbit.oak.spi.security.authentication.external.TestIdentityProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Set;

import static org.apache.jackrabbit.oak.spi.security.authentication.external.TestIdentityProvider.ID_SECOND_USER;
import static org.apache.jackrabbit.oak.spi.security.authentication.external.TestIdentityProvider.ID_TEST_USER;
import static org.junit.Assert.assertFalse;

public class PrincipalResolutionTest extends DynamicSyncContextTest {

    @Override
    @NotNull
    protected ExternalIdentityProvider createIDP() {
        return new PrincipalResolvingIDP();
    }

    private static class PrincipalResolvingIDP extends TestIdentityProvider implements PrincipalNameResolver {

        @NotNull
        @Override
        public String fromExternalIdentityRef(@NotNull ExternalIdentityRef externalIdentityRef) throws ExternalIdentityException {
            ExternalIdentity identity = getIdentity(externalIdentityRef);
            if (identity == null) {
                throw new ExternalIdentityException();
            } else {
                return identity.getPrincipalName();
            }
        }
    }

    /**
     * With {@code PrincipalNameResolver} the extra verification for all member-refs being groups is omitted.
     * @throws Exception
     */
    @Test
    public void testSyncMembershipWithUserRef() throws Exception {
        TestIdentityProvider.TestUser testuser = (TestIdentityProvider.TestUser) idp.getUser(ID_TEST_USER);
        Set<ExternalIdentityRef> groupRefs = ImmutableSet.copyOf(testuser.getDeclaredGroups());

        ExternalUser second = idp.getUser(ID_SECOND_USER);
        testuser.withGroups(second.getExternalId());
        assertFalse(Iterables.elementsEqual(groupRefs, testuser.getDeclaredGroups()));

        sync(testuser, SyncResult.Status.ADD);

        assertDynamicMembership(testuser, 1);
    }
}
