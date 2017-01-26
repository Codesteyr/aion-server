package ai;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_BINDSTONE_ALREADY_REGISTERED;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_BINDSTONE_IS_ATTACKED;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_BINDSTONE_IS_DESTROYED;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_BINDSTONE_IS_REMOVED;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_REGISTER_BINDSTONE_FULL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_REGISTER_BINDSTONE_HAVE_NO_AUTHORITY;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.services.KiskService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Source
 */
@AIName("kisk")
public class KiskAI extends NpcAI {

	private final int CANCEL_DIALOG_METERS = 5;

	@Override
	public Kisk getOwner() {
		return (Kisk) super.getOwner();
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (getLifeStats().isFullyRestoredHp())
			for (Player member : getOwner().getCurrentMemberList())
				PacketSendUtility.sendPacket(member, STR_BINDSTONE_IS_ATTACKED());
	}

	@Override
	protected void handleDied() {
		getOwner().broadcastPacket(STR_BINDSTONE_IS_DESTROYED());
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		KiskService.getInstance().removeKisk(getOwner());
		if (!isAlreadyDead())
			getOwner().broadcastPacket(STR_BINDSTONE_IS_REMOVED());

		super.handleDespawned();
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getKisk() == getOwner()) {
			PacketSendUtility.sendPacket(player, STR_BINDSTONE_ALREADY_REGISTERED());
			return;
		}

		if (getOwner().canBind(player)) {
			AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_REGISTER_BINDSTONE, getOwner().getObjectId(), CANCEL_DIALOG_METERS,
				new AIRequest() {

					private boolean decisionTaken = false;

					@Override
					public void acceptRequest(Creature requester, Player responder, int requestId) {
						if (!decisionTaken) {
							// Check again if it's full (If they waited to press OK)
							if (!getOwner().canBind(responder)) {
								PacketSendUtility.sendPacket(responder, STR_CANNOT_REGISTER_BINDSTONE_HAVE_NO_AUTHORITY());
								return;
							}
							KiskService.getInstance().onBind(getOwner(), responder);
						}
					}

					@Override
					public void denyRequest(Creature requester, Player responder) {
						decisionTaken = true;
					}
				});

		} else if (getOwner().getCurrentMemberCount() >= getOwner().getMaxMembers())
			PacketSendUtility.sendPacket(player, STR_CANNOT_REGISTER_BINDSTONE_FULL());
		else
			PacketSendUtility.sendPacket(player, STR_CANNOT_REGISTER_BINDSTONE_HAVE_NO_AUTHORITY());
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}