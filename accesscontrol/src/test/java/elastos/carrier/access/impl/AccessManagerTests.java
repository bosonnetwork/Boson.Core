/*
 * Copyright (c) 2022 - 2023 trinity-tech.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elastos.carrier.access.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.google.common.collect.Maps;

import elastos.carrier.CarrierException;
import elastos.carrier.Configuration;
import elastos.carrier.DefaultConfiguration;
import elastos.carrier.Id;
import elastos.carrier.Node;
import elastos.carrier.access.Permission.Access;
import elastos.carrier.utils.ThreadLocals;

public class AccessManagerTests {
	private static String repoPath = System.getProperty("java.io.tmpdir") + File.separator + "AccessManagerTests";
	private static File defaultsDir;
	private static File aclsDir;

	private static EnumMap<Subscription, AccessControlList> defaultACLs;
	private static EnumMap<Subscription, List<Id>> subscriptions;

	private static Node node;
	private static AccessManager am;

	private static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children)
				deleteFile(child);
		}

		file.delete();
	}

	static void setupDefaults() throws IOException {
		File repoDir = new File(repoPath);
		if (repoDir.exists())
			deleteFile(repoDir);

		repoDir.mkdirs();

		defaultsDir = new File(repoDir, "defaults");
		defaultsDir.mkdirs();

		aclsDir = new File(repoDir, "acls");
		aclsDir.mkdirs();

		defaultACLs = new EnumMap<>(Subscription.class);

		// Free
		Map<String, Permission> permissions = new HashMap<>();

		var svc = "test.service.foo";
		var access = Access.Allow;

		Map<String, Object> props = new HashMap<>();
		props.put("connnections", 8);
		props.put("domain", false);
		props.put("port", 8018);

		var perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.bar";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("ratePerMinute", 4);
		props.put("ratePerMHour", 30);
		props.put("ratePerDay", 240);
		props.put("lookup", "optimistic");

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.xyz";

		props = new HashMap<>();
		props.put("ipv6", false);
		props.put("log", "info");

		perm = new Permission(svc, Access.Allow, props);
		permissions.put(perm.getTargetServiceId(), perm);

		var subscription = Subscription.Free;
		var acl = new AccessControlList(subscription, permissions);
		acl.seal();
		defaultACLs.put(subscription, acl);
		ThreadLocals.ObjectMapper().writeValue(new File(defaultsDir, subscription.name()), acl);

		// Professional
		permissions = new HashMap<>();

		svc = "test.service.foo";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("connnections", 16);
		props.put("domain", true);
		props.put("port", 8018);

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.bar";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("ratePerMinute", 8);
		props.put("ratePerMHour", 60);
		props.put("ratePerDay", 320);
		props.put("lookup", "optimistic");

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.xyz";

		props = new HashMap<>();
		props.put("ipv6", false);
		props.put("log", "debug");

		perm = new Permission(svc, Access.Allow, props);
		permissions.put(perm.getTargetServiceId(), perm);

		subscription = Subscription.Professional;
		acl = new AccessControlList(subscription, permissions);
		acl.seal();
		defaultACLs.put(subscription, acl);
		ThreadLocals.ObjectMapper().writeValue(new File(defaultsDir, subscription.name()), acl);

		// Premium
		permissions = new HashMap<>();

		svc = "test.service.foo";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("connnections", 48);
		props.put("domain", true);
		props.put("port", 8018);

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.bar";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("ratePerMinute", 8);
		props.put("ratePerMHour", 60);
		props.put("ratePerDay", 600);
		props.put("lookup", "conservative");

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.xyz";

		props = new HashMap<>();
		props.put("ipv6", true);
		props.put("log", "trace");

		perm = new Permission(svc, Access.Allow, props);
		permissions.put(perm.getTargetServiceId(), perm);

		subscription = Subscription.Premium;
		acl = new AccessControlList(subscription, permissions);
		acl.seal();
		defaultACLs.put(subscription, acl);
		ThreadLocals.ObjectMapper().writeValue(new File(defaultsDir, subscription.name()), acl);
	}

	static void setupTestACLs() throws IOException {
		AccessManager am = new AccessManager(repoPath);

		subscriptions = new EnumMap<>(Subscription.class);
		for (var s : EnumSet.allOf(Subscription.class)) {
			int total = ThreadLocals.random().nextInt(1, 16);
			List<Id> nodes = new ArrayList<>(total);

			for (var i = 0; i < total; i++) {
				Id node = Id.random();
				am.allow(node, s);

				nodes.add(node);
			}

			subscriptions.put(s, nodes);
		}
	}

	static Configuration getNodeConfiguration() {
		DefaultConfiguration.Builder dcb = new DefaultConfiguration.Builder();
		dcb.setAutoIPAddress(true)
			.setAutoIPv6Address(false)
			.setListeningPort(10099);

		return dcb.build();
	}

	@BeforeAll
	static void setup() throws Exception {
		setupDefaults();
		setupTestACLs();

		am = new AccessManager(repoPath);
		node = new elastos.carrier.kademlia.Node(getNodeConfiguration());
		am.init(node);

		node.start();
	}

	@AfterAll
	static void teardown() {
		node.stop();
	}

	static boolean equals(elastos.carrier.access.Permission a, elastos.carrier.access.Permission b) {
		return a.getTargetServiceId().equals(b.getTargetServiceId()) &&
				a.getAccess() == b.getAccess() &&
				Maps.difference(a.getProperties(), b.getProperties()).areEqual();
	}

	@Test
	void testPredefinedPermissions() throws CarrierException, IOException {
		for (var s : EnumSet.allOf(Subscription.class)) {
			boolean allow = s != Subscription.Blocked;
			Access access = allow ? Access.Allow : Access.Deny;

			List<Id> nodes = subscriptions.get(s);
			for (Id nid : nodes) {
				var acl = am.get(nid);
				assertNotNull(acl);
				assertEquals(s, acl.getSubscription());
				assertTrue(acl.getPermissions().isEmpty());

				var svc = "test.service.foo";
				assertEquals(allow, am.allow(nid, svc));
				var perm = am.getPermission(nid, svc);
				assertNotNull(perm);
				assertEquals(svc, perm.getTargetServiceId());
				assertEquals(access, perm.getAccess());
				assertEquals(allow, perm.isAllow());
				if (s == Subscription.Blocked) {
					var props = perm.getProperties();
					assertTrue(props.isEmpty());
				} else {
					var expected = defaultACLs.get(s).getPermission(svc);
					assertTrue(equals(expected, perm));
				}

				svc = "test.service.bar";
				assertEquals(allow, am.allow(nid, svc));
				perm = am.getPermission(nid, svc);
				assertNotNull(perm);
				assertEquals(svc, perm.getTargetServiceId());
				assertEquals(access, perm.getAccess());
				assertEquals(allow, perm.isAllow());
				if (s == Subscription.Blocked) {
					var props = perm.getProperties();
					assertTrue(props.isEmpty());
				} else {
					var expected = defaultACLs.get(s).getPermission(svc);
					assertTrue(equals(expected, perm));
				}

				svc = "test.service.xyz";
				assertEquals(allow, am.allow(nid, svc));
				perm = am.getPermission(nid, svc);
				assertNotNull(perm);
				assertEquals(svc, perm.getTargetServiceId());
				assertEquals(access, perm.getAccess());
				assertEquals(allow, perm.isAllow());
				if (s == Subscription.Blocked) {
					var props = perm.getProperties();
					assertTrue(props.isEmpty());
				} else {
					var expected = defaultACLs.get(s).getPermission(svc);
					assertTrue(equals(expected, perm));
				}
			}
		}
	}

	@Test
	void testUndefinedNode() throws IOException {
		Id nid = Id.random();

		var svc = "test.service.foo";
		assertEquals(true, am.allow(nid, svc));
		var perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		var expected = defaultACLs.get(Subscription.Free).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.bar";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(Subscription.Free).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.xyz";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(Subscription.Free).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.abc";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		assertTrue(perm.getProperties().isEmpty());
	}

	@Test
	@EnabledIfSystemProperty(named = "elastos.carrier.enviroment", matches = "development")
	void testUpdateNodeACL() throws IOException, InterruptedException {
		Id nid = Id.random();

		AccessManager lam = new AccessManager(repoPath);
		Subscription subscription =  Subscription.Professional;
		lam.allow(nid, subscription);

		var svc = "test.service.foo";
		assertEquals(true, am.allow(nid, svc));
		var perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		var expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.bar";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.xyz";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		subscription =  Subscription.Premium;
		lam.allow(nid, subscription);
		TimeUnit.SECONDS.sleep(70);

		svc = "test.service.foo";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.bar";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.xyz";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		subscription =  Subscription.Blocked;
		lam.deny(nid);
		TimeUnit.SECONDS.sleep(70);

		svc = "test.service.foo";
		assertEquals(false, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Deny, perm.getAccess());
		assertEquals(true, perm.isDeny());
		assertTrue(perm.getProperties().isEmpty());

		svc = "test.service.bar";
		assertEquals(false, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Deny, perm.getAccess());
		assertEquals(true, perm.isDeny());
		assertTrue(perm.getProperties().isEmpty());

		svc = "test.service.xyz";
		assertEquals(false, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Deny, perm.getAccess());
		assertEquals(true, perm.isDeny());
		assertTrue(perm.getProperties().isEmpty());
	}

	AccessControlList updateFreeACL() throws IOException {
		Map<String, Permission> permissions = new HashMap<>();

		var svc = "test.service.foo";
		var access = Access.Allow;

		Map<String, Object> props = new HashMap<>();
		props.put("connnections", 4);
		props.put("domain", false);
		props.put("port", 9019);

		var perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.bar";
		access = Access.Allow;

		props = new HashMap<>();
		props.put("ratePerMinute", 4);
		props.put("ratePerMHour", 30);
		props.put("ratePerDay", 120);
		props.put("lookup", "arbitrary");

		perm = new Permission(svc, access, props);
		permissions.put(perm.getTargetServiceId(), perm);

		svc = "test.service.xyz";

		props = new HashMap<>();
		props.put("ipv6", false);
		props.put("log", "warn");

		perm = new Permission(svc, Access.Allow, props);
		permissions.put(perm.getTargetServiceId(), perm);

		var subscription = Subscription.Free;
		var acl = new AccessControlList(subscription, permissions);
		acl.seal();
		ThreadLocals.ObjectMapper().writeValue(new File(defaultsDir, subscription.name()), acl);
		return acl;
	}

	@Test
	@EnabledIfSystemProperty(named = "elastos.carrier.enviroment", matches = "development")
	void testUpdateDefaultAcl() throws IOException, InterruptedException {
		Id nid = Id.random();

		Subscription subscription =  Subscription.Free;

		var svc = "test.service.foo";
		assertEquals(true, am.allow(nid, svc));
		var perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		var expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.bar";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.xyz";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		expected = defaultACLs.get(subscription).getPermission(svc);
		assertTrue(equals(expected, perm));

		// change the default acl for Free
		var newFreeAcl = updateFreeACL();
		TimeUnit.SECONDS.sleep(70);

		svc = "test.service.foo";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		var defaultPerm = defaultACLs.get(subscription).getPermission(svc);
		assertFalse(equals(defaultPerm, perm));
		expected = newFreeAcl.getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.bar";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		defaultPerm = defaultACLs.get(subscription).getPermission(svc);
		assertFalse(equals(defaultPerm, perm));
		expected = newFreeAcl.getPermission(svc);
		assertTrue(equals(expected, perm));

		svc = "test.service.xyz";
		assertEquals(true, am.allow(nid, svc));
		perm = am.getPermission(nid, svc);
		assertNotNull(perm);
		assertEquals(svc, perm.getTargetServiceId());
		assertEquals(Access.Allow, perm.getAccess());
		assertEquals(true, perm.isAllow());
		defaultPerm = defaultACLs.get(subscription).getPermission(svc);
		assertFalse(equals(defaultPerm, perm));
		expected = newFreeAcl.getPermission(svc);
		assertTrue(equals(expected, perm));
	}
}
