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

package io.bosonnetwork.service;

import io.bosonnetwork.Id;

/**
 * Receives client changes through {@link ClientContext#onChange(ClientListener)}. Override only the
 * callbacks a service needs; all default implementations do nothing. Callbacks identify what to
 * invalidate, and services query the context or authorizer for current information.
 * <p>
 * Callbacks may run on arbitrary threads and must neither block nor throw. A service with an event
 * loop should hand notifications to it. See {@link ClientContext#onChange(ClientListener)} for
 * subscription lifecycle and delivery guarantees.
 */
public interface ClientListener {
	/**
	 * A user was created or explicitly updated.
	 *
	 * @param userId the user
	 */
	default void onUserUpdated(Id userId) {}

	/**
	 * A user was removed, including all of its devices. Separate device-removal callbacks are not
	 * required for this removal.
	 *
	 * @param userId the user
	 */
	default void onUserRemoved(Id userId) {}

	/**
	 * A device was added or explicitly updated. Routine last-seen/address touches are excluded.
	 *
	 * @param userId the owner
	 * @param deviceId the device
	 */
	default void onDeviceUpdated(Id userId, Id deviceId) {}

	/**
	 * One device was removed. The owner is retained because the removed device can no longer be
	 * looked up through the context.
	 *
	 * @param userId the owner
	 * @param deviceId the device
	 */
	default void onDeviceRemoved(Id userId, Id deviceId) {}

	/**
	 * One user's effective subscription entitlements may have changed. Reauthorize that user.
	 *
	 * @param userId the user
	 */
	default void onUserPlanChanged(Id userId) {}

	/**
	 * Shared plan metadata or features relevant to this service may have changed. The host filters
	 * service-specific changes before dispatch. Reauthorize the service's relevant users; this
	 * callback does not identify a particular user or plan.
	 */
	default void onPlansChanged() {}
}
