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

package io.bosonnetwork.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.bosonnetwork.utils.AddressUtils.Subnet;

public class AddressUtilsTests {
	@Test
	public void testGlobalUnicastMatcher() throws UnknownHostException {
		assertTrue(AddressUtils.isGlobalUnicast(InetAddress.getByName("8.8.8.8")));
		assertTrue(AddressUtils.isGlobalUnicast(InetAddress.getByName("2001:4860:4860::8888")));
		// wildcard
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("0.0.0.0")));
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("::0")));
		// loopback
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("127.0.0.15")));
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("::1")));
		// private/LL
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("192.168.13.47")));
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("169.254.1.0")));
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("fe80::")));
		// ULA
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("fc00::")));
		assertFalse(AddressUtils.isGlobalUnicast(InetAddress.getByName("fd00::")));
	}

	@Test
	public void testMappedBypass() throws UnknownHostException {
		byte[] v4mapped = new byte[16];
		v4mapped[11] = (byte) 0xff;
		v4mapped[10] = (byte) 0xff;

		assertTrue(InetAddress.getByAddress(v4mapped) instanceof Inet4Address);
		assertTrue(AddressUtils.fromBytesVerbatim(v4mapped) instanceof Inet6Address);

	}

	@Test
	public void netMaskTest() throws UnknownHostException {
		Subnet everything = new Subnet(InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 }), 0);
		Subnet single = new Subnet(InetAddress.getByAddress(new byte[] { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA }), 32);
		Subnet nibbleA = new Subnet(InetAddress.getByAddress(new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00 }), 4);
		Subnet firstByte = new Subnet(InetAddress.getByAddress(new byte[] { (byte) 0xA5, (byte) 0x00, (byte) 0x00, (byte) 0x00 }), 8);

		assertTrue(everything.contains(InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 })));
		assertTrue(everything.contains(InetAddress.getByAddress(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff })));
		assertTrue(single.contains(InetAddress.getByAddress(new byte[] { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA })));
		assertTrue(nibbleA.contains(InetAddress.getByAddress(new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00 })));
		assertTrue(firstByte.contains(InetAddress.getByAddress(new byte[] { (byte) 0xA5, (byte) 0x00, (byte) 0x00, (byte) 0x00 })));
		assertTrue(firstByte.contains(InetAddress.getByAddress(new byte[] { (byte) 0xA5, (byte) 0xff, (byte) 0xff, (byte) 0xff })));
	}

	@Test
	public void testIsBogon() throws Exception {
		InetAddress addr = InetAddress.getByName("151.101.2.132");
		boolean result = AddressUtils.isBogon(addr, 1234);
		assertFalse(result);

		addr = InetAddress.getByName("192.168.1.1");
		result = AddressUtils.isBogon(addr, 1234);
		assertTrue(result);

		addr = InetAddress.getByName("10.0.0.1");
		result = AddressUtils.isBogon(addr, 1234);
		assertTrue(result);

		addr = InetAddress.getByName("127.0.0.1");
		result = AddressUtils.isBogon(addr, 1234);
		assertTrue(result);
	}

	@Test
	public void getAllAddress() {
		List<InetAddress> addrs = AddressUtils.getAllAddresses().collect(Collectors.toList());

		addrs.forEach((a) -> {
			System.out.println(a);
		});

		assertFalse(addrs.isEmpty());
	}

	@Test
	public void getDefaultAddress() {
		List<InetAddress> newBindAddrs = AddressUtils.getAllAddresses().filter(Inet4Address.class::isInstance)
				.filter((a) -> AddressUtils.isAnyUnicast(a))
				.distinct()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));

		newBindAddrs.forEach((a) -> {
			System.out.println(a);
		});

		assertFalse(newBindAddrs.isEmpty());
	}
}
