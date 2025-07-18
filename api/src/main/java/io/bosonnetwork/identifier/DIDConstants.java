/*
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

package io.bosonnetwork.identifier;

public class DIDConstants {
	public static final String DID_SCHEME = "did";
	public static final String DID_METHOD = "boson";

	public static final String W3C_DID_CONTEXT = "https://www.w3.org/ns/did/v1.1";
	public static final String BOSON_DID_CONTEXT = "https://bosonnetwork.io/ns/did/v1";

	public static final String W3C_VC_CONTEXT = "https://www.w3.org/ns/credentials/v2";
	public static final String BOSON_VC_CONTEXT = "https://bosonnetwork.io/ns/credentials/v1";

	public static final String W3C_ED25519_CONTEXT = "https://w3id.org/security/suites/ed25519-2020/v1";

	public static final String DEFAULT_VC_TYPE = "VerifiableCredential";
	public static final String DEFAULT_VP_TYPE = "VerifiablePresentation";

	protected static final String DEFAULT_VERIFICATION_METHOD_FRAGMENT = "default";

	public static final Object BOSON_ID_FORMAT_W3C = new Object();
}