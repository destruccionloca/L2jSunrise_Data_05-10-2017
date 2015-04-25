/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.IceQueenCastleExtreme;

import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;
import ai.npc.AbstractNpcAI;

public class IceQueenCastleExtreme extends AbstractNpcAI
{
	private class IQCEWorld extends InstanceWorld
	{
		public L2Attackable _freyaThrone = null;
		public L2Npc _freyaSpelling = null;
		public L2Attackable _jinia = null;
		public L2Attackable _kegor = null;
		public boolean isMovieNow = false;
		public FastMap<Integer, L2Npc> _simple_knights = new FastMap<>();
		public FastMap<Integer, L2Npc> _glaciers = new FastMap<>();
		
		public L2Attackable _freyaStand_hard = null;
		public L2Attackable _glakias_hard = null;
		public FastMap<Integer, L2Npc> _archery_knights_hard = new FastMap<>();
		
		public IQCEWorld()
		{
			InstanceManager.getInstance();
		}
	}
	
	private static boolean debugWarnings = false;
	
	// Npc
	private static final int FREYA_THRONE = 29177; // First freya
	private static final int FREYA_SPELLING = 29178; // Second freya
	private static final int FREYA_STAND = 29180; // Last freya
	// private static final int INVISIBLE_NPC = 18919;
	private static final int KNIGHT = 18856; // Archery Knight
	private static final int GLACIER = 18853; // GLACIER
	private static final int BREATH = 18854; // Archer's Breath
	private static final int GLAKIAS = 25700; // Glakias (Archery Knight Captain)
	private static final int SIRRA = 32762; // Sirra
	private static final int JINIA = 32781; // Jinia
	private static final int SUPP_JINIA = 18850; // Jinia
	private static final int SUPP_KEGOR = 18851; // Kegor
	// Skills
	private static final SkillHolder JINIA_SUPPORT = new SkillHolder(6288, 1); // Jinia's Prayer
	private static final SkillHolder KEGOR_SUPPORT = new SkillHolder(6289, 1); // Kegor's Courage
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	// Misc
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4; // Wednesday
	private static final int RESET_DAY_2 = 7; // Saturday
	private static final int TEMPLATE_ID = 144; // Ice Queen's Castle
	private static final int DOOR_ID = 23140101;
	
	//@formatter:off
	private static final Location FREYA_SPAWN = new Location(114720, -117085, -11088, 15956);
	private static final Location FREYA_SPELLING_SPAWN = new Location(114723, -117502, -10672, 15956);
	private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
	private static final Location GLAKIAS_SPAWN = new Location(114707, -114799, -11199, 15956);
	private static final Location SUPP_JINIA_SPAWN = new Location(114751, -114781, -11205);
	private static final Location SUPP_KEGOR_SPAWN = new Location(114659, -114796, -11205);
	private static final Location[] ENTER_LOC =
	{
		new Location(114185, -112435, -11210),
		new Location(114183, -112280, -11210),
		new Location(114024, -112435, -11210),
		new Location(114024, -112278, -11210),
		new Location(113865, -112435, -11210),
		new Location(113865, -112276, -11210),
	
	};
	
	public static final int[] archery_blocked_status =
	{
		11, 19, 22, 29, 39
	};
	
	public static final int[] glacier_blocked_status =
	{
		11, 19, 29, 39
	};
	
	public static final int[][] frozeKnightsSpawn =
	{
		{ 113845, -116091, -11168, 8264 },
		{ 113381, -115622, -11168, 8264 },
		{ 113380, -113978, -11168, -8224 },
		{ 113845, -113518, -11168, -8224 },
		{ 115591, -113516, -11168, -24504 },
		{ 116053, -113981, -11168, -24504 },
		{ 116061, -115611, -11168, 24804 },
		{ 115597, -116080, -11168, 24804 },
		{ 112942, -115480, -10960, 52 },
		{ 112940, -115146, -10960, 52 },
		{ 112945, -114453, -10960, 52 },
		{ 112945, -114123, -10960, 52 },
		{ 116497, -114117, -10960, 32724 },
		{ 116499, -114454, -10960, 32724 },
		{ 116501, -115145, -10960, 32724 },
		{ 116502, -115473, -10960, 32724 }
	};
	
	public static final int[][] _archeryKnightsSpawn =
	{
		{ 114713, -115109, -11202, 16456 },
		{ 114008, -115080, -11202, 3568 },
		{ 114422, -115508, -11202, 12400 },
		{ 115023, -115508, -11202, 20016 },
		{ 115459, -115079, -11202, 27936 }
	};
	//@formatter:on
	
	public IceQueenCastleExtreme()
	{
		super(IceQueenCastleExtreme.class.getSimpleName(), "instances");
		addStartNpc(SIRRA, SUPP_KEGOR, SUPP_JINIA);
		addFirstTalkId(SUPP_KEGOR, SUPP_JINIA);
		addTalkId(JINIA);
		
		addSpawnId(GLACIER, BREATH);
		addKillId(FREYA_THRONE, FREYA_SPELLING, GLACIER);
		addAggroRangeEnterId(BREATH);
		
		addAttackId(KNIGHT, FREYA_STAND);
		addKillId(KNIGHT, FREYA_STAND, GLAKIAS);
		addSpawnId(KNIGHT);
		addAggroRangeEnterId(KNIGHT);
	}
	
	private class spawnWave implements Runnable
	{
		private final int _waveId;
		private final IQCEWorld _world;
		
		public spawnWave(int waveId, int instanceId)
		{
			_waveId = waveId;
			_world = getWorld(instanceId);
		}
		
		@Override
		public void run()
		{
			if (_world == null)
			{
				return;
			}
			
			switch (_waveId)
			{
				case 1:
					// Sirra
					spawnNpc(SIRRA, 114766, -113141, -11200, 15956, _world.getInstanceId());
					handleWorldState(1, _world.getInstanceId());
					break;
				case 3:
					if (Util.contains(archery_blocked_status, _world.getStatus()))
					{
						break;
					}
					if ((_world._archery_knights_hard.size() < 5) && (_world.getStatus() < 44))
					{
						L2Attackable mob = null;
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						mob = (L2Attackable) spawnNpc(KNIGHT, spawnXY[0], spawnXY[1], -11200, 20016, _world.getInstanceId());
						mob.setOnKillDelay(0);
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.setRunning();
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						_world._archery_knights_hard.put(mob.getObjectId(), mob);
						
						if ((_world.getStatus() == 1) || (_world.getStatus() == 11) || (_world.getStatus() == 24) || (_world.getStatus() == 30) || (_world.getStatus() == 40))
						{
							mob.setIsImmobilized(true);
						}
					}
					break;
				case 5:
					if ((_world._glaciers.size() < 5) && (_world.getStatus() < 44) && !Util.contains(glacier_blocked_status, _world.getStatus()))
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Attackable mob = (L2Attackable) spawnNpc(GLACIER, spawnXY[0], spawnXY[1], -11200, 20016, _world.getInstanceId());
						_world._glaciers.put(mob.getObjectId(), mob);
					}
					if (_world.getStatus() < 44)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, _world.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
					}
					break;
				case 6:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Attackable mob = null;
						mob = (L2Attackable) spawnNpc(KNIGHT, iter[0], iter[1], iter[2], iter[3], _world.getInstanceId());
						mob.setOnKillDelay(0);
						mob.setRunning();
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						_world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					handleWorldState(_world.getStatus() + 1, _world);
					break;
				case 7:
					handleWorldState(2, _world.getInstanceId());
					break;
				case 9:
					handleWorldState(19, _world.getInstanceId());
					break;
				case 10:
					handleWorldState(20, _world.getInstanceId());
					break;
				case 11:
					handleWorldState(25, _world.getInstanceId());
					break;
				case 12:
					handleWorldState(30, _world.getInstanceId());
					break;
				case 13:
					handleWorldState(31, _world.getInstanceId());
					break;
				case 14:
					handleWorldState(41, _world.getInstanceId());
					break;
				case 15:
					handleWorldState(43, _world.getInstanceId());
					break;
				case 16:
					setInstanceRestriction(_world);
					InstanceManager.getInstance().getInstance(_world.getInstanceId()).setDuration(300000);
					InstanceManager.getInstance().getInstance(_world.getInstanceId()).setEmptyDestroyTime(0);
					handleWorldState(45, _world.getInstanceId());
					break;
				case 19:
					stopAll(_world);
					break;
				case 20:
					_world.isMovieNow = false;
					startAll(_world);
					break;
				case 21:
					handleWorldState(47, _world.getInstanceId());
					break;
			}
		}
	}
	
	private void broadcastMovie(int movieId, IQCEWorld world)
	{
		world.isMovieNow = true;
		
		stopAll(world);
		
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			player.showQuestMovie(movieId);
		}
		
		int pause = 0;
		
		switch (movieId)
		{
			case 15:
				pause = 53500;
				break;
			case 16:
				pause = 21100;
				break;
			case 17:
				pause = 21500;
				break;
			case 18:
				pause = 27000;
				break;
			case 19:
				pause = 16000;
				break;
			case 23:
				pause = 7000;
				break;
			case 20:
				pause = 55500;
				break;
			default:
				pause = 0;
		}
		
		if (movieId != 15)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.getInstanceId()), pause);
		}
		if (movieId == 19)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 100);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 200);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 500);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 1000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 2000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 3000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 4000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 5000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 6000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 7000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 8000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 9000);
		}
	}
	
	protected void handleWorldState(int statusId, int instanceId)
	{
		IQCEWorld world = getWorld(instanceId);
		if (world != null)
		{
			handleWorldState(statusId, world);
		}
		else if (debugWarnings)
		{
			_log.warn(IceQueenCastleExtreme.class.getSimpleName() + ": Not Found world at handleWorldState(int, int).");
		}
	}
	
	protected void handleWorldState(int statusId, IQCEWorld world)
	{
		int instanceId = world.getInstanceId();
		
		switch (statusId)
		{
			case 0:
				break;
			case 1:
				broadcastMovie(15, world);
				InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(DOOR_ID).openMe();
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.getInstanceId()), 52500);
				break;
			case 2:
				world._freyaThrone = (L2GrandBossInstance) addSpawn(FREYA_THRONE, FREYA_SPAWN, false, 0, true, world.getInstanceId());
				world._freyaThrone.setIsNoRndWalk(true);
				world._freyaThrone.setisReturningToSpawnPoint(false);
				world._freyaThrone.setOnKillDelay(0);
				world._freyaThrone.setIsInvul(true);
				world._freyaThrone.setIsImmobilized(true);
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					player.getKnownList().addKnownObject(world._freyaThrone);
				}
				
				for (int[] iter : frozeKnightsSpawn)
				{
					L2Attackable mob = null;
					mob = (L2Attackable) spawnNpc(KNIGHT, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					world._simple_knights.put(mob.getObjectId(), mob);
				}
				
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					mob = (L2Attackable) spawnNpc(KNIGHT, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					world._archery_knights_hard.put(mob.getObjectId(), mob);
				}
				
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					player.setIsImmobilized(false);
					player.setIsInvul(false);
				}
				
				world.isMovieNow = false;
				break;
			case 10:
				manageScreenMsg(world, NpcStringId.BEGIN_STAGE_1_FREYA);
				InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(DOOR_ID).closeMe();
				world._freyaThrone.setIsInvul(false);
				world._freyaThrone.setIsImmobilized(false);
				world._freyaThrone.setIsRunning(true);
				world._freyaThrone.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
				
				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(GLACIER, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				
				for (L2Npc mob : world._archery_knights_hard.values())
				{
					archeryAttack(mob, world);
				}
				break;
			case 11:
				broadcastMovie(16, world);
				
				for (L2Npc mob : world._archery_knights_hard.values())
				{
					mob.deleteMe();
				}
				
				world._archery_knights_hard.clear();
				world._freyaThrone.deleteMe();
				world._freyaThrone = null;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(9, world.getInstanceId()), 22000);
				break;
			case 12:
				break;
			case 19:
				world._freyaSpelling = addSpawn(FREYA_SPELLING, FREYA_SPELLING_SPAWN, false, 0, true, world.getInstanceId());
				world._freyaSpelling.setIsImmobilized(true);
				manageTimer(world, 60);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(10, world.getInstanceId()), 60000);
				break;
			case 20:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					mob = (L2Attackable) spawnNpc(KNIGHT, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					world._archery_knights_hard.put(mob.getObjectId(), mob);
				}
				break;
			case 21:
				manageScreenMsg(world, NpcStringId.BEGIN_STAGE_2_FREYA);
				
				for (L2Npc mob : world._archery_knights_hard.values())
				{
					archeryAttack(mob, world);
				}
				
				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(GLACIER, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				break;
			case 22:
			case 23:
				break;
			case 24:
				broadcastMovie(23, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(11, world.getInstanceId()), 7000);
				break;
			case 25:
				world._glakias_hard = (L2RaidBossInstance) addSpawn(GLAKIAS, GLAKIAS_SPAWN, false, 0, true, world.getInstanceId());
				world._glakias_hard.setOnKillDelay(0);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				break;
			case 29:
				manageTimer(world, 60);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(12, world.getInstanceId()), 60000);
				break;
			case 30:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					mob = (L2Attackable) spawnNpc(KNIGHT, iter[0], iter[1], iter[2], iter[3], instanceId);
					mob.setOnKillDelay(0);
					world._archery_knights_hard.put(mob.getObjectId(), mob);
				}
				
				if (world._freyaSpelling != null)
				{
					world._freyaSpelling.deleteMe();
					world._freyaSpelling = null;
				}
				broadcastMovie(17, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(13, world.getInstanceId()), 21500);
				break;
			case 31:
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					player.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED);
				}
				
				manageScreenMsg(world, NpcStringId.BEGIN_STAGE_3_FREYA);
				world._freyaStand_hard = (L2GrandBossInstance) addSpawn(FREYA_STAND, FREYA_SPAWN, false, 0, true, world.getInstanceId());
				world._freyaStand_hard.setOnKillDelay(0);
				world._freyaStand_hard.setIsRunning(true);
				world._freyaStand_hard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
				
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					player.getKnownList().addKnownObject(world._freyaStand_hard);
				}
				break;
			case 40:
				broadcastMovie(18, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(14, world.getInstanceId()), 27000);
				break;
			case 41:
				for (L2Npc mob : world._archery_knights_hard.values())
				{
					archeryAttack(mob, world);
				}
				
				world._jinia = (L2QuestGuardInstance) addSpawn(SUPP_JINIA, SUPP_JINIA_SPAWN, false, 0, true, world.getInstanceId());
				world._jinia.setIsRunning(true);
				world._jinia.setAutoAttackable(false);
				world._jinia.setIsInvul(true);
				world._jinia.setCanReturnToSpawnPoint(false);
				world._kegor = (L2QuestGuardInstance) addSpawn(SUPP_KEGOR, SUPP_KEGOR_SPAWN, false, 0, true, world.getInstanceId());
				world._kegor.setIsRunning(true);
				world._kegor.setAutoAttackable(false);
				world._kegor.setIsInvul(true);
				world._kegor.setCanReturnToSpawnPoint(false);
				handleWorldState(42, instanceId);
				break;
			case 42:
				manageScreenMsg(world, NpcStringId.BEGIN_STAGE_4_FREYA);
				
				if ((world._freyaStand_hard != null) && !world._freyaStand_hard.isDead())
				{
					world._jinia.setTarget(world._freyaStand_hard);
					world._jinia.setRunning();
					world._jinia.addDamageHate(world._freyaStand_hard, 0, 9999);
					world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
					world._kegor.setTarget(world._freyaStand_hard);
					world._kegor.setRunning();
					world._kegor.addDamageHate(world._freyaStand_hard, 0, 9999);
					world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
				}
				else
				{
					world._jinia.setIsImmobilized(true);
					world._kegor.setIsImmobilized(true);
				}
				
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					JINIA_SUPPORT.getSkill().getEffects(world._jinia, player);
					KEGOR_SUPPORT.getSkill().getEffects(world._kegor, player);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(15, instanceId), 6000);
				break;
			case 43:
				break;
			case 44:
				broadcastMovie(19, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(16, instanceId), 20000);
			case 45:
				broadcastMovie(20, world);
				handleWorldState(46, instanceId);
			case 46:
				for (L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
				{
					if (mob.getId() != FREYA_STAND)
					{
						mob.deleteMe();
						InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(21, instanceId), 58500);
				break;
			case 47:
				for (L2PcInstance player : getAllPlayersInside(world))
				{
					player.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
				}
				break;
			default:
				if (debugWarnings)
				{
					_log.warn(IceQueenCastleExtreme.class.getSimpleName() + ": Not handled world status - " + statusId);
				}
				break;
		}
		world.setStatus(statusId);
	}
	
	public L2PcInstance getRandomPlayer(IQCEWorld world)
	{
		boolean exists = false;
		while (!exists)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(world.getAllowed().get(Rnd.get(0, world.getAllowed().size() - 1)));
			if (player != null)
			{
				exists = true;
				return player;
			}
		}
		return null;
	}
	
	protected IQCEWorld getWorld(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		IQCEWorld world = null;
		if (tmpworld instanceof IQCEWorld)
		{
			world = (IQCEWorld) tmpworld;
		}
		
		if ((world == null) && debugWarnings)
		{
			_log.warn(IceQueenCastleExtreme.class.getSimpleName() + ": World not found in getWorld(int instanceId)");
		}
		return world;
	}
	
	private IQCEWorld getWorld(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		IQCEWorld world = null;
		if (tmpworld instanceof IQCEWorld)
		{
			world = (IQCEWorld) tmpworld;
		}
		
		if ((world == null) && debugWarnings)
		{
			_log.warn(IceQueenCastleExtreme.class.getSimpleName() + ": World not found in getWorld(L2PcInstance player)");
		}
		return world;
	}
	
	private int getWorldStatus(L2PcInstance player)
	{
		try
		{
			return getWorld(player).getStatus();
		}
		catch (Exception e)
		{
			if (debugWarnings)
			{
				_log.warn(IceQueenCastleExtreme.class.getSimpleName() + ": getWorldStatus is Null");
			}
			return 0;
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		switch (npc.getId())
		{
			case KNIGHT:
				if (npc.getDisplayEffect() == 1)
				{
					npc.setDisplayEffect(2);
				}
				
				if (getWorldStatus(attacker) == 2)
				{
					handleWorldState(10, attacker.getInstanceId());
				}
				else if (getWorldStatus(attacker) == 20)
				{
					handleWorldState(21, attacker.getInstanceId());
				}
				break;
			case FREYA_STAND:
				if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
				{
					if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
					{
						npc.setTarget(attacker);
						npc.doCast(ANTI_STRIDER.getSkill());
					}
				}
				
				double cur_hp = npc.getCurrentHp();
				double max_hp = npc.getMaxHp();
				int percent = (int) Math.round((cur_hp / max_hp) * 100);
				if ((percent <= 20) && (getWorldStatus(attacker) < 40))
				{
					handleWorldState(40, attacker.getInstanceId());
				}
				break;
			case FREYA_THRONE:
				if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
				{
					if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
					{
						npc.setTarget(attacker);
						npc.doCast(ANTI_STRIDER.getSkill());
					}
				}
				
				if (!npc.isCastingNow())
				{
					callSkillAI(npc);
				}
				break;
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((tmpworld != null) && (tmpworld instanceof IQCEWorld))
		{
			final IQCEWorld world = (IQCEWorld) tmpworld;
			switch (npc.getId())
			{
				case GLAKIAS:
					handleWorldState(29, killer.getInstanceId());
					break;
				case FREYA_STAND:
					handleWorldState(44, killer.getInstanceId());
					break;
				case FREYA_THRONE:
					handleWorldState(11, killer.getInstanceId());
					break;
				case KNIGHT:
					if (world._archery_knights_hard.containsKey(npc.getObjectId()))
					{
						world._archery_knights_hard.remove(npc.getObjectId());
						
						if ((world.getStatus() > 20) && (world.getStatus() < 24))
						{
							if (world._archery_knights_hard.size() == 0)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 30000);
							}
						}
						else if (world.getStatus() < 44)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (getRandom(10, 40) * 1000) + 30000);
						}
					}
					else if (world._simple_knights.containsKey(npc.getObjectId()))
					{
						world._simple_knights.remove(npc.getObjectId());
						startQuestTimer("spawndeco_" + npc.getSpawn().getX() + "_" + npc.getSpawn().getY() + "_" + npc.getSpawn().getZ() + "_" + npc.getSpawn().getHeading() + "_" + npc.getInstanceId(), 30000, null, null);
					}
					break;
				case GLACIER:
					world._glaciers.remove(npc.getObjectId());
					npc.setDisplayEffect(3);
					L2Npc mob = spawnNpc(BREATH, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId());
					mob.setRunning();
					mob.setTarget(killer);
					((L2Attackable) mob).addDamageHate(killer, 0, 99999);
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
					break;
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((tmpworld != null) && (tmpworld instanceof IQCEWorld))
		{
			// final IQCNBWorld world = (IQCNBWorld) tmpworld;
			switch (npc.getId())
			{
				case SUPP_JINIA:
					player.sendPacket(ActionFailed.STATIC_PACKET);
					break;
			/**
			 * case SUPP_KEGOR: if (world.isSupportActive) { player.sendPacket(ActionFailed.STATIC_PACKET); } break;
			 */
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		IQCEWorld world = getWorld(npc.getInstanceId());
		if ((world != null) && (world.getStatus() >= 44))
		{
			npc.deleteMe();
		}
		
		if ((world != null) && world.isMovieNow && (npc instanceof L2Attackable))
		{
			npc.abortAttack();
			npc.abortCast();
			npc.setIsImmobilized(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		if (npc.getId() == GLACIER)
		{
			npc.setDisplayEffect(1);
			npc.setIsImmobilized(true);
			((L2Attackable) npc).setOnKillDelay(0);
			startQuestTimer("setDisplayEffect2", 1000, npc, null);
			startQuestTimer("cast", 20000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world instanceof IQCEWorld)
			{
				player.stopAllEffectsExceptThoseThatLastThroughDeath();
				if (player.hasSummon())
				{
					player.getSummon().stopAllEffectsExceptThoseThatLastThroughDeath();
				}
				
				teleportPlayer(player, (IQCEWorld) world);
				return;
			}
			player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return;
		}
		
		if (checkConditions(player))
		{
			world = new IQCEWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(TEMPLATE_ID);
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_log.info("Freya extreme started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			
			L2Party party = player.getParty();
			if (player.isGM())
			{
				if (party != null)
				{
					int count = 1;
					for (L2PcInstance plr : party.getMembers())
					{
						world.addAllowed(plr.getObjectId());
						_log.info("Freya Party Member " + count + ", Name is: " + plr.getName());
						count++;
						teleportPlayer(plr, (IQCEWorld) world);
					}
				}
				else
				{
					world.addAllowed(player.getObjectId());
					teleportPlayer(player, (IQCEWorld) world);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.getInstanceId()), 100);
				return;
			}
			
			if ((party != null) && party.isInCommandChannel())
			{
				int count = 1;
				for (L2PcInstance plr : party.getCommandChannel().getMembers())
				{
					world.addAllowed(plr.getObjectId());
					_log.info("Freya Party Member " + count + ", Name is: " + plr.getName());
					count++;
					teleportPlayer(plr, (IQCEWorld) world);
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.getInstanceId()), 100);
				return;
			}
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		final L2CommandChannel channel = party != null ? party.getCommandChannel() : null;
		
		if (player.isGM() && player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		else if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (player != channel.getLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_PLAYERS_TO_HARD)
		{
			player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2793).addInt(10));
			return false;
		}
		
		if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_PLAYERS_TO_HARD)
		{
			player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2102));
			return false;
		}
		
		for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
		{
			QuestState st = partyMember.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
			if ((st == null) || !st.isCompleted())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			if (partyMember.getLevel() < Config.MIN_PLAYER_LEVEL_TO_HARD)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), TEMPLATE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportPlayer(L2PcInstance player, IQCEWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		teleportPlayer(player, ENTER_LOC[getRandom(ENTER_LOC.length)], world.getInstanceId(), false);
		if (player.hasSummon())
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.getInstanceId());
			player.getSummon().teleToLocation(113991, -112297, -11200);
		}
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "IceQueenCastleExtreme.xml");
		}
		else if (event.startsWith("spawndeco"))
		{
			String[] params = event.split("_");
			IQCEWorld world = getWorld(Integer.parseInt(params[5]));
			if ((world != null) && (world.getStatus() < 44))
			{
				L2Attackable mob = null;
				mob = (L2Attackable) spawnNpc(KNIGHT, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
				mob.setIsImmobilized(true);
				mob.setDisplayEffect(1);
				world._simple_knights.put(mob.getObjectId(), mob);
			}
		}
		else if (event.equalsIgnoreCase("setDisplayEffect2"))
		{
			if (!npc.isDead())
			{
				npc.setDisplayEffect(2);
			}
		}
		else if (event.equalsIgnoreCase("show_string"))
		{
			if ((npc != null) && !npc.isDead())
			{
				manageScreenMsg(npc.getInstanceId(), NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE);
			}
		}
		else if (event.equalsIgnoreCase("summon_breathe"))
		{
			L2Npc mob = spawnNpc(BREATH, npc.getX() + getRandom(-90, 90), npc.getY() + getRandom(-90, 90), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			if (npc.getTarget() != null)
			{
				mob.setTarget(npc.getTarget());
				((L2Attackable) mob).addDamageHate((L2Character) npc.getTarget(), 0, 99999);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc.getTarget());
			}
		}
		else if (event.equalsIgnoreCase("cast"))
		{
			if ((npc != null) && !npc.isDead())
			{
				L2Skill skill = SkillData.getInstance().getInfo(6437, getRandom(1, 3));
				for (L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(skill.getAffectRange()))
				{
					if (!plr.isAffectedBySkill(6437) && !plr.isDead() && !plr.isAlikeDead())
					{
						skill.getEffects(npc, plr);
					}
				}
				startQuestTimer("cast", 20000, npc, null);
			}
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc.getId() == BREATH) || (npc.getId() == KNIGHT))
		{
			if (npc.isImmobilized())
			{
				npc.abortAttack();
				npc.abortCast();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	public int[] getRandomPoint(int min_x, int max_x, int min_y, int max_y)
	{
		int[] ret =
		{
			0,
			0
		};
		ret[0] = Rnd.get(min_x, max_x);
		ret[1] = Rnd.get(min_y, max_y);
		return ret;
	}
	
	private void archerySpawn(L2Npc mob)
	{
		((L2Attackable) mob).setOnKillDelay(0);
		mob.setDisplayEffect(1);
		mob.setIsImmobilized(true);
	}
	
	private void archeryAttack(L2Npc mob, IQCEWorld world)
	{
		mob.setDisplayEffect(2);
		mob.setIsImmobilized(false);
		mob.setRunning();
		L2PcInstance victim = getRandomPlayer(world);
		mob.setTarget(victim);
		((L2Attackable) mob).addDamageHate(victim, 0, 9999);
		mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
	}
	
	public void stopAll(IQCEWorld world)
	{
		if (world == null)
		{
			return;
		}
		
		if ((world._freyaStand_hard != null) && !world._freyaStand_hard.isDead())
		{
			if (world._freyaStand_hard.getTarget() != null)
			{
				world._freyaStand_hard.abortAttack();
				world._freyaStand_hard.abortCast();
				world._freyaStand_hard.setTarget(null);
				world._freyaStand_hard.clearAggroList();
				world._freyaStand_hard.setIsImmobilized(true);
				world._freyaStand_hard.teleToLocation(world._freyaStand_hard.getX() - 100, world._freyaStand_hard.getY() + 100, world._freyaStand_hard.getZ(), world._freyaStand_hard.getHeading(), false);
			}
		}
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.getInstanceId()).getNpcs())
		{
			if ((mob != null) && !mob.isDead())
			{
				mob.abortAttack();
				mob.abortCast();
				if (mob instanceof L2Attackable)
				{
					((L2Attackable) mob).clearAggroList();
				}
				mob.setIsImmobilized(true);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.setIsImmobilized(true);
			player.setIsInvul(true);
		}
	}
	
	public void startAll(IQCEWorld world)
	{
		if (world == null)
		{
			return;
		}
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.getInstanceId()).getNpcs())
		{
			L2Object target = null;
			
			if (mob.getTarget() != null)
			{
				target = mob.getTarget();
			}
			else
			{
				target = getRandomPlayer(world);
			}
			
			if ((mob.getId() != GLACIER) && !world._simple_knights.containsKey(mob.getObjectId()) && (mob instanceof L2Attackable))
			{
				((L2Attackable) mob).addDamageHate((L2Character) target, 0, 9999);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				mob.setIsImmobilized(false);
			}
		}
		
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			player.setIsImmobilized(false);
			if (player.getFirstEffect(L2EffectType.INVINCIBLE) == null)
			{
				player.setIsInvul(false);
			}
		}
	}
	
	//@formatter:off
	private void callSkillAI(L2Npc mob)
	{
		int[][] freya_skills =
		{
			{ 6274, 1, 4000, 10 },
			{ 6276, 1, -1, 100 },
			{ 6277, 1, -1, 100 },
			{ 6278, 1, -1, 100 },
			{ 6279, 1, 2000, 100 },
			{ 6282, 1, -1, 100 }
		};
		
		int iter = getRandom(0, 2);
		
		if ((freya_skills[iter][3] < 100) && (getRandom(100) > freya_skills[iter][3]))
		{
			iter = 3;
		}
		
		mob.doCast(SkillData.getInstance().getInfo(freya_skills[iter][0], freya_skills[iter][1]));
		if (freya_skills[iter][2] > 0)
		{
			startQuestTimer("show_string", freya_skills[iter][2], mob, null);
		}
		
		if (freya_skills[iter][0] == 6277)
		{
			startQuestTimer("summon_breathe", 10000, mob, null);
		}
	}
	//@formatter:on
	
	private void manageTimer(IQCEWorld world, int time)
	{
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			player.sendPacket(new ExSendUIEvent(player, false, false, time, 0, "Time remaining until next battle"));
		}
	}
	
	private void manageScreenMsg(int instanceId, NpcStringId stringId)
	{
		IQCEWorld world = getWorld(instanceId);
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			showOnScreenMsg(player, stringId, 2, 6000);
		}
	}
	
	private void manageScreenMsg(IQCEWorld world, NpcStringId stringId)
	{
		for (L2PcInstance player : getAllPlayersInside(world))
		{
			showOnScreenMsg(player, stringId, 2, 6000);
		}
	}
	
	private List<L2PcInstance> getAllPlayersInside(IQCEWorld world)
	{
		List<L2PcInstance> _players = new FastList<>();
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if ((player != null) && (player.getInstanceId() == world.getInstanceId()))
			{
				_players.add(player);
			}
		}
		return _players;
	}
	
	protected void setInstanceRestriction(IQCEWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		Calendar.getInstance().set(Calendar.MINUTE, RESET_MIN);
		Calendar.getInstance().set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
		sm.addInstanceName(TEMPLATE_ID);
		// set instance reenter time for all allowed players
		for (int objectId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, TEMPLATE_ID, reenter.getTimeInMillis());
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
}