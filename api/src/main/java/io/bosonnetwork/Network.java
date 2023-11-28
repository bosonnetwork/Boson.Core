/*
 * Copyright (c) 2022 - 2023 trinity-tech.io
 * Copyright (c) 2023 -      bosonnetwork.io
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

package io.bosonnetwork;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;

public enum Network {
	IPv4(StandardProtocolFamily.INET, Inet4Address.class, 20 + 8, 1450),
	IPv6(StandardProtocolFamily.INET6, Inet6Address.class, 40 + 8, 1200);

	private final ProtocolFamily protocolFamily;
	private final Class<? extends InetAddress> preferredAddressType;
	private final int protocolHeaderSize;
	private final int maxPacketSize;

	private Network(ProtocolFamily family, Class<? extends InetAddress> addresstype, int headerSize, int maxPacketSize) {
		this.protocolFamily = family;
		this.preferredAddressType = addresstype;
		this.protocolHeaderSize = headerSize;
		this.maxPacketSize = maxPacketSize;
	}

	public boolean canUseSocketAddress(InetSocketAddress addr) {
		return canUseAddress(addr.getAddress());
	}

	public boolean canUseAddress(InetAddress addr) {
		return preferredAddressType.isInstance(addr);
	}

	public static Network of(InetSocketAddress addr) {
		return of(addr.getAddress());
	}

	public static Network of(InetAddress addr) {
		return (addr instanceof Inet4Address) ? IPv4 : IPv6;
	}

	ProtocolFamily protocolFamily() {
		return protocolFamily;
	}

	public int protocolHeaderSize() {
		return protocolHeaderSize;
	}

	public int maxPacketSize() {
		return maxPacketSize;
	}

	@Override
	public String toString() {
		return name();
	}
}