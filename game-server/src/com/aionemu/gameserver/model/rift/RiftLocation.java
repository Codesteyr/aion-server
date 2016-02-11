package com.aionemu.gameserver.model.rift;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.rift.RiftTemplate;

/**
 * @author Source
 */
public class RiftLocation {

	private boolean opened;
	protected RiftTemplate template;
	private List<VisibleObject> spawned = new FastTable<>();

	public RiftLocation() {
	}

	public RiftLocation(RiftTemplate template) {
		this.template = template;
	}

	public int getId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}

	public boolean hasSpawns() {
		return template.hasSpawns();
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean state) {
		opened = state;
	}

	public List<VisibleObject> getSpawned() {
		return spawned;
	}

}
