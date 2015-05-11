package org.kurento.tree.server.treemanager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.kurento.client.IceCandidate;
import org.kurento.jsonrpc.Session;
import org.kurento.tree.client.TreeEndpoint;
import org.kurento.tree.client.TreeException;

public abstract class AbstractNTreeTM implements TreeManager {

	public abstract class TreeInfo {

		public abstract void release();

		public abstract String setTreeSource(Session session, String offerSdp);

		public abstract void removeTreeSource();

		public abstract TreeEndpoint addTreeSink(Session session,
				String sdpOffer);

		public abstract void removeTreeSink(String sinkId);

		public abstract void addSinkIceCandidate(String sinkId,
				IceCandidate iceCandidate);

		public abstract void addTreeIceCandidate(IceCandidate iceCandidate);
	}

	private final TreeInfo DUMMY_TREE_INFO = new TreeInfo() {
		@Override
		public void release() {
		}

		@Override
		public String setTreeSource(Session session, String offerSdp) {
			return null;
		}

		@Override
		public void removeTreeSource() {
		}

		@Override
		public TreeEndpoint addTreeSink(Session session, String sdpOffer) {
			return null;
		}

		@Override
		public void removeTreeSink(String sinkId) {
		}

		@Override
		public void addSinkIceCandidate(String sinkId,
				IceCandidate iceCandidate) {
		}

		@Override
		public void addTreeIceCandidate(IceCandidate iceCandidate) {
		}
	};

	private ConcurrentHashMap<String, TreeInfo> trees = new ConcurrentHashMap<>();

	public AbstractNTreeTM() {
	}

	@Override
	public String createTree() throws TreeException {

		String treeId = UUID.randomUUID().toString();
		trees.put(treeId, createTreeInfo(treeId));
		return treeId;
	}

	protected abstract TreeInfo createTreeInfo(String treeId);

	@Override
	public synchronized void createTree(String treeId)
			throws TreeException {

		TreeInfo prevTreeInfo = trees.putIfAbsent(treeId, DUMMY_TREE_INFO);
		if (prevTreeInfo != null) {
			throw new TreeException("Tree with id '" + treeId
					+ "' already exists. Try another one");
		} else {
			trees.replace(treeId, createTreeInfo(treeId));
		}
	}

	@Override
	public synchronized void releaseTree(String treeId) throws TreeException {
		getTreeInfo(treeId).release();
		trees.remove(treeId);
	}

	@Override
	public synchronized String setTreeSource(Session session, String treeId,
			String offerSdp) throws TreeException {
		return getTreeInfo(treeId).setTreeSource(session, offerSdp);
	}

	@Override
	public synchronized void removeTreeSource(String treeId)
			throws TreeException {
		getTreeInfo(treeId).removeTreeSource();
	}

	@Override
	public synchronized TreeEndpoint addTreeSink(Session session,
			String treeId, String sdpOffer) throws TreeException {
		return getTreeInfo(treeId).addTreeSink(session, sdpOffer);
	}

	@Override
	public synchronized void removeTreeSink(String treeId, String sinkId)
			throws TreeException {
		getTreeInfo(treeId).removeTreeSink(sinkId);
	}

	@Override
	public synchronized void addSinkIceCandidate(String treeId, String sinkId,
			IceCandidate iceCandidate) {
		getTreeInfo(treeId).addSinkIceCandidate(sinkId, iceCandidate);
	}

	@Override
	public synchronized void addTreeIceCandidate(String treeId,
			IceCandidate iceCandidate) {
		getTreeInfo(treeId).addTreeIceCandidate(iceCandidate);
	}

	protected TreeInfo getTreeInfo(String treeId) {
		TreeInfo treeInfo = trees.get(treeId);
		if (treeInfo == null) {
			throw new TreeException("Tree with id '" + treeId + "' not found");
		} else {
			return treeInfo;
		}
	}
}