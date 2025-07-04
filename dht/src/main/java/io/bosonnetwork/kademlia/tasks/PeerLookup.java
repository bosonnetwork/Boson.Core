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

package io.bosonnetwork.kademlia.tasks;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bosonnetwork.Id;
import io.bosonnetwork.Network;
import io.bosonnetwork.NodeInfo;
import io.bosonnetwork.PeerInfo;
import io.bosonnetwork.kademlia.Constants;
import io.bosonnetwork.kademlia.DHT;
import io.bosonnetwork.kademlia.KClosestNodes;
import io.bosonnetwork.kademlia.RPCCall;
import io.bosonnetwork.kademlia.messages.FindPeerRequest;
import io.bosonnetwork.kademlia.messages.FindPeerResponse;
import io.bosonnetwork.kademlia.messages.Message;

/**
 * @hidden
 */
public class PeerLookup extends LookupTask {
	Consumer<Collection<PeerInfo>> resultHandler;

	private static final Logger log = LoggerFactory.getLogger(PeerLookup.class);

	public PeerLookup(DHT dht, Id target) {
		super(dht, target);
	}

	public void setResultHandler(Consumer<Collection<PeerInfo>> resultHandler) {
		this.resultHandler = resultHandler;
	}

	@Override
	protected void prepare() {
		KClosestNodes kns = new KClosestNodes(getDHT(), getTarget(), Constants.MAX_ENTRIES_PER_BUCKET * 2);
		kns.fill();
		addCandidates(kns.entries());
	}

	@Override
	protected void update() {
		while (canDoRequest()) {
			CandidateNode cn = getNextCandidate();
			if (cn == null)
				return;

			FindPeerRequest q = new FindPeerRequest(getTarget());

			q.setWant4(getDHT().getType() == Network.IPv4);
			q.setWant6(getDHT().getType() == Network.IPv6);

			sendCall(cn, q, (c) -> cn.setSent());
		}
	}

	@Override
	protected void callResponsed(RPCCall call, Message response) {
		super.callResponsed(call, response);

		if (!call.matchesId())
			return; // Ignore

		if (response.getType() != Message.Type.RESPONSE || response.getMethod() != Message.Method.FIND_PEER)
			return;

		FindPeerResponse r = (FindPeerResponse) response;
		if (r.hasPeers()) {
			List<PeerInfo> peers = r.getPeers();
			for (PeerInfo peer : peers) {
				if (!peer.isValid()) {
					log.error("Response include invalid peer, signature mismatch");
					return;
				}
			}

			if (resultHandler != null)
				resultHandler.accept(peers);

		} else {
			List<NodeInfo> nodes = r.getNodes(getDHT().getType());
			if (nodes.isEmpty())
				return;

			addCandidates(nodes);
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}