/*
 * Copyright (c) 2026 -      bosonnetwork.io
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

package io.bosonnetwork.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.bosonnetwork.Id;
import io.bosonnetwork.crypto.CryptoIdentity;
import io.bosonnetwork.service.ClientContext;
import io.bosonnetwork.service.ClientDevice;
import io.bosonnetwork.service.ClientListener;
import io.bosonnetwork.service.ClientUser;

class StaticClientContextListenerTests {
	private StaticClientContext context;

	private record DeviceChange(Id userId, Id deviceId) {}

	private static class RecordingListener implements ClientListener {
		final List<Id> userUpdates = new CopyOnWriteArrayList<>();
		final List<Id> userRemovals = new CopyOnWriteArrayList<>();
		final List<DeviceChange> deviceUpdates = new CopyOnWriteArrayList<>();
		final List<DeviceChange> deviceRemovals = new CopyOnWriteArrayList<>();
		final List<Id> userPlanChanges = new CopyOnWriteArrayList<>();
		final AtomicInteger sharedPlanChanges = new AtomicInteger();

		@Override
		public void onUserUpdated(Id userId) { userUpdates.add(userId); }

		@Override
		public void onUserRemoved(Id userId) { userRemovals.add(userId); }

		@Override
		public void onDeviceUpdated(Id userId, Id deviceId) { deviceUpdates.add(new DeviceChange(userId, deviceId)); }

		@Override
		public void onDeviceRemoved(Id userId, Id deviceId) { deviceRemovals.add(new DeviceChange(userId, deviceId)); }

		@Override
		public void onUserPlanChanged(Id userId) { userPlanChanges.add(userId); }

		@Override
		public void onPlansChanged() { sharedPlanChanges.incrementAndGet(); }

		int total() {
			return userUpdates.size() + userRemovals.size() + deviceUpdates.size() + deviceRemovals.size()
					+ userPlanChanges.size() + sharedPlanChanges.get();
		}
	}

	@BeforeEach
	void setUp() {
		context = new StaticClientContext(new CryptoIdentity());
	}

	@Test
	void callbacksReceiveTheirIdsAndSeePostMutationState() {
		Id user = Id.random();
		Id device = Id.random();
		List<String> calls = new ArrayList<>();
		ClientContext view = context;
		view.onChange(new ClientListener() {
			@Override
			public void onUserUpdated(Id userId) {
				assertEquals(user, userId);
				assertTrue(view.getUser(userId).join().isPresent());
				calls.add("userUpdated");
			}

			@Override
			public void onDeviceUpdated(Id userId, Id deviceId) {
				assertEquals(user, userId);
				assertEquals(device, deviceId);
				assertTrue(view.getDevice(userId, deviceId).join().isPresent());
				calls.add("deviceUpdated");
			}

			@Override
			public void onUserPlanChanged(Id userId) {
				assertEquals(user, userId);
				assertEquals(Map.of("maxChannels", 4), context.getFeatures(userId));
				calls.add("userPlanChanged");
			}

			@Override
			public void onDeviceRemoved(Id userId, Id deviceId) {
				assertEquals(user, userId);
				assertEquals(device, deviceId);
				assertFalse(view.existsDevice(userId, deviceId).join());
				calls.add("deviceRemoved");
			}

			@Override
			public void onUserRemoved(Id userId) {
				assertEquals(user, userId);
				assertFalse(view.existsUser(userId).join());
				calls.add("userRemoved");
			}
		});
		assertTrue(context.addUser(user, "Alice"));
		assertTrue(context.addDevice(user, device, "Phone", "Test"));
		context.setFeatures(user, Map.of("maxChannels", 4));
		assertTrue(context.removeDevice(user, device));
		assertTrue(context.removeUser(user));
		assertEquals(List.of("userUpdated", "deviceUpdated", "userPlanChanged", "deviceRemoved", "userRemoved"), calls);
	}

	@Test
	void explicitUpdatesKeepDevicesFeaturesAndDeviceOwnership() {
		Id user = Id.random();
		Id device = Id.random();
		Id other = Id.random();
		context.addUser(user, "Alice");
		context.addUser(other, "Bob");
		context.addDevice(user, device, "Phone", "Test");
		context.setFeatures(user, Map.of("maxChannels", 4));
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		ClientUser replacement = new PlainUser(user, "Alicia", null, null, null);
		assertTrue(context.updateUser(replacement));
		assertFalse(context.updateUser(replacement));
		assertSame(replacement, context.getUserSync(user).orElseThrow());
		assertTrue(context.existsDeviceSync(user, device));
		assertEquals(4, context.getFeatures(user).get("maxChannels"));
		ClientDevice updated = new PlainDevice(device, user, "Tablet", "NewApp");
		assertTrue(context.updateDevice(updated));
		assertFalse(context.updateDevice(updated));
		assertFalse(context.updateDevice(new PlainDevice(device, other, "Wrong owner", "Test")));
		assertFalse(context.updateUser(new PlainUser(Id.random())));
		assertFalse(context.updateDevice(new PlainDevice(Id.random(), user)));
		assertSame(updated, context.getDeviceSync(user, device).orElseThrow());
		assertEquals(List.of(user), listener.userUpdates);
		assertEquals(List.of(new DeviceChange(user, device)), listener.deviceUpdates);
		assertEquals(2, listener.total());
	}

	@Test
	void failedAndNoOpMutationsDoNotNotify() {
		Id user = Id.random();
		Id device = Id.random();
		context.addUser(user, "Alice");
		context.addDevice(user, device, "Phone", "Test");
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		assertFalse(context.addUser(user, "Duplicate"));
		assertFalse(context.addDevice(user, device, "Duplicate", "Test"));
		assertFalse(context.removeUser(Id.random()));
		assertFalse(context.removeDevice(user, Id.random()));
		assertFalse(context.removeDevice(Id.random(), device));
		assertThrows(IllegalArgumentException.class, () -> context.addDevice(Id.random(), Id.random(), "Phone", "Test"));
		context.setFeatures(user, Map.of());
		assertEquals(0, listener.total());
		context.setFeatures(user, Map.of("maxChannels", 4));
		context.setFeatures(user, Map.of("maxChannels", 4));
		context.setFeatures(user, Map.of());
		context.setFeatures(user, Map.of());
		assertEquals(List.of(user, user), listener.userPlanChanges);
		assertEquals(2, listener.total());
	}

	@Test
	void userRemovalClearsAllAdmissionAndFeaturesBeforeOneCallback() {
		Id user = Id.random();
		Id phone = Id.random();
		Id tablet = Id.random();
		context.addUser(user, "Alice");
		context.addDevice(user, phone, "Phone", "Test");
		context.addDevice(user, tablet, "Tablet", "Test");
		context.setFeatures(user, Map.of("maxChannels", 4));
		RecordingListener listener = new RecordingListener() {
			@Override
			public void onUserRemoved(Id userId) {
				assertFalse(context.getAuthenticator().authenticateUser(userId).join());
				assertFalse(context.getAuthenticator().authenticateDevice(userId, phone, "localhost").join());
				assertFalse(context.existsDeviceSync(userId, tablet));
				assertTrue(context.getFeatures(userId).isEmpty());
				super.onUserRemoved(userId);
			}
		};
		ClientContext.Subscription subscription = context.onChange(listener);
		assertTrue(context.removeUser(user));
		assertFalse(context.removeUser(user));
		assertEquals(List.of(user), listener.userRemovals);
		assertEquals(1, listener.total());
		subscription.close();
		context.addUser(user, "Alice again");
		assertTrue(context.getFeatures(user).isEmpty());
	}

	@Test
	void clearRemovesAllStateBeforeCallbacksAndKeepsListeners() {
		Id first = Id.random();
		Id second = Id.random();
		context.addUser(first, "Alice");
		context.addUser(second, "Bob");
		context.addDevice(first, Id.random(), "Phone", "Test");
		context.setFeatures(second, Map.of("maxChannels", 4));
		RecordingListener listener = new RecordingListener() {
			@Override
			public void onUserRemoved(Id userId) {
				assertFalse(context.existsUserSync(first));
				assertFalse(context.existsUserSync(second));
				assertTrue(context.getDevicesSync(first).isEmpty());
				assertTrue(context.getFeatures(second).isEmpty());
				super.onUserRemoved(userId);
			}
		};
		context.onChange(listener);
		context.clear();
		context.clear();
		assertEquals(2, listener.total());
		assertEquals(Set.of(first, second), Set.copyOf(listener.userRemovals));
		context.addUser(first, "Alice again");
		assertEquals(List.of(first), listener.userUpdates);
		assertEquals(3, listener.total());
	}

	@Test
	void featureOnlyFixturesCanBeConfiguredAndCleared() {
		Id user = Id.random();
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		context.setFeatures(user, Map.of("maxChannels", 4));
		assertFalse(context.existsUserSync(user));
		context.clear();
		context.clear();
		assertTrue(context.getFeatures(user).isEmpty());
		assertEquals(List.of(user, user), listener.userPlanChanges);
		assertEquals(2, listener.total());
	}

	@Test
	void listenerFailureDoesNotFailMutationOrSkipOtherListeners() {
		context.onChange(new ClientListener() {
			@Override
			public void onUserUpdated(Id userId) { throw new IllegalStateException("Listener test failure"); }

			@Override
			public void onUserRemoved(Id userId) { throw new IllegalStateException("Listener test failure"); }
		});
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		Id user = Id.random();
		assertTrue(context.addUser(user, "Alice"));
		assertTrue(context.removeUser(user));
		assertEquals(List.of(user), listener.userUpdates);
		assertEquals(List.of(user), listener.userRemovals);
		assertEquals(2, listener.total());
	}

	@Test
	void servicesCanOverrideOnlyTheCallbacksTheyNeed() {
		List<Id> removedUsers = new ArrayList<>();
		context.onChange(new ClientListener() {
			@Override
			public void onUserRemoved(Id userId) { removedUsers.add(userId); }
		});
		Id user = Id.random();
		Id device = Id.random();
		context.addUser(user, "Alice");
		context.addDevice(user, device, "Phone", "Test");
		context.setFeatures(user, Map.of("maxChannels", 4));
		context.removeDevice(user, device);
		assertTrue(removedUsers.isEmpty());
		context.removeUser(user);
		assertEquals(List.of(user), removedUsers);
	}

	@Test
	void repeatedRegistrationsHaveIndependentIdempotentSubscriptions() {
		RecordingListener listener = new RecordingListener();
		ClientContext.Subscription first = context.onChange(listener);
		ClientContext.Subscription second = context.onChange(listener);
		Id user = Id.random();
		context.setFeatures(user, Map.of("maxChannels", 4));
		assertEquals(2, listener.total());
		first.close();
		first.close();
		context.setFeatures(user, Map.of());
		assertEquals(3, listener.total());
		second.close();
		second.close();
		context.setFeatures(user, Map.of("maxChannels", 5));
		assertEquals(3, listener.total());
	}

	@Test
	void listenersCanUnsubscribeThemselvesDuringDispatch() {
		AtomicReference<ClientContext.Subscription> subscription = new AtomicReference<>();
		AtomicInteger calls = new AtomicInteger();
		subscription.set(context.onChange(new ClientListener() {
			@Override
			public void onUserPlanChanged(Id userId) {
				calls.incrementAndGet();
				subscription.get().close();
			}
		}));
		RecordingListener other = new RecordingListener();
		context.onChange(other);
		Id user = Id.random();
		context.setFeatures(user, Map.of("maxChannels", 4));
		context.setFeatures(user, Map.of());
		assertEquals(1, calls.get());
		assertEquals(List.of(user, user), other.userPlanChanges);
	}

	@Test
	void registrationDoesNotReplayAndNullListenersAreRejected() {
		context.addUser(Id.random(), "Alice");
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		assertEquals(0, listener.total());
		assertThrows(NullPointerException.class, () -> context.onChange(null));
		ClientContext allowAll = ClientContext.allowAll(new CryptoIdentity());
		assertThrows(NullPointerException.class, () -> allowAll.onChange(null));
		ClientContext.Subscription subscription = allowAll.onChange(listener);
		assertTrue(allowAll.existsUser(Id.random()).join());
		subscription.close();
		subscription.close();
		assertEquals(0, listener.total());
	}

	@Test
	void concurrentUserAddsReportOnlyTheSuccessfulInsertion() throws Exception {
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		Id user = Id.random();
		List<Boolean> results = race(() -> context.addUser(user, "Alice"), () -> context.addUser(user, "Bob"));
		assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
		assertEquals(List.of(user), listener.userUpdates);
		assertEquals(1, listener.total());
	}

	@Test
	void concurrentDeviceAddsPreserveGlobalOwnershipAndReportOnce() throws Exception {
		Id first = Id.random();
		Id second = Id.random();
		Id device = Id.random();
		context.addUser(first, "Alice");
		context.addUser(second, "Bob");
		RecordingListener listener = new RecordingListener();
		context.onChange(listener);
		List<Boolean> results = race(() -> context.addDevice(first, device, "Phone", "Test"),
				() -> context.addDevice(second, device, "Phone", "Test"));
		assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
		assertEquals(1, context.getDevicesSync(first).size() + context.getDevicesSync(second).size());
		Id owner = results.get(0) ? first : second;
		assertEquals(List.of(new DeviceChange(owner, device)), listener.deviceUpdates);
		assertEquals(1, listener.total());
	}

	private static List<Boolean> race(Callable<Boolean> first, Callable<Boolean> second) throws Exception {
		var executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try {
			var futures = List.of(first, second).stream().map(operation -> executor.submit(() -> {
				ready.countDown();
				if (!start.await(5, TimeUnit.SECONDS))
					throw new AssertionError("Concurrent mutations did not start");
				return operation.call();
			})).toList();
			assertTrue(ready.await(5, TimeUnit.SECONDS));
			start.countDown();
			return List.of(futures.get(0).get(5, TimeUnit.SECONDS), futures.get(1).get(5, TimeUnit.SECONDS));
		} finally {
			start.countDown();
			executor.shutdownNow();
		}
	}
}
