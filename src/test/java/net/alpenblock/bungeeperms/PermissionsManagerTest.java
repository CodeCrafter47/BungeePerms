package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.platform.Sender;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class PermissionsManagerTest extends AbstractTest
{
    @Test
    public void testReload() throws Exception
    {
        platform.simulateUserJoin("CodeCrafter47");
        pm.addUserPerm(pm.getUser("CodeCrafter47"), "some.permission");
        assertTrue(pm.getUser("CodeCrafter47").hasPerm("some.permission"));
        pm.reload();
        assertTrue(pm.getUser("CodeCrafter47").hasPerm("some.permission"));
    }

    @Test
    public void testValidateUsersGroups() throws Exception
    {
        Group group = createGroup("test");
        group.getInheritances().add("test2");
        pm.addGroup(group);
        pm.validateUsersGroups();
        assertTrue(pm.getGroup("test").getInheritances().isEmpty());

        Sender player = platform.simulateUserJoin("TestPlayer");
        pm.addUserGroup(pm.getUser(player.getName()), pm.getGroup("test"));
        pm.getUser(player.getName()).getGroups().add(createGroup("test2"));
        pm.validateUsersGroups();
        assertEquals(1, pm.getUser(player.getName()).getGroups().size());
        assertTrue(pm.getUser(player.getName()).getGroups().get(0).getName().equals("test"));
    }

    @Test
    public void testGetMainGroup() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setWeight(1);
        b.setWeight(5);

        pm.addGroup(a);
        pm.addGroup(b);

        Sender player = platform.simulateUserJoin("TestPlayer");

        pm.addUserGroup(pm.getUser(player.getName()), a);
        pm.addUserGroup(pm.getUser(player.getName()), b);

        assertEquals("a", pm.getMainGroup(pm.getUser(player.getName())).getName());

        Sender player2 = platform.simulateUserJoin("TestPlayer2");

        assertNull(pm.getMainGroup(pm.getUser(player2.getName()))); // expect null as player2 has no groups assigned
    }

    @Test
    public void testGetNextGroup() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");
        Group c = createGroup("c");
        Group d = createGroup("d");

        a.setLadder("ladder");
        b.setLadder("ladder");
        c.setLadder("ladder");
        d.setLadder("default");

        a.setRank(1000);
        b.setRank(100);
        c.setRank(10);
        d.setRank(1);

        pm.addGroup(a);
        pm.addGroup(b);
        pm.addGroup(c);
        pm.addGroup(d);

        assertEquals(b, pm.getNextGroup(a));
        assertEquals(c, pm.getNextGroup(b));
        assertNull(pm.getNextGroup(c));
        assertNull(pm.getNextGroup(d));
    }

    @Test
    public void testGetPreviousGroup() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");
        Group c = createGroup("c");
        Group d = createGroup("d");

        a.setLadder("ladder");
        b.setLadder("ladder");
        c.setLadder("ladder");
        d.setLadder("default");

        a.setRank(1);
        b.setRank(10);
        c.setRank(100);
        d.setRank(1000);

        pm.addGroup(a);
        pm.addGroup(b);
        pm.addGroup(c);
        pm.addGroup(d);

        assertEquals(b, pm.getPreviousGroup(a));
        assertEquals(c, pm.getPreviousGroup(b));
        assertNull(pm.getPreviousGroup(c));
        assertNull(pm.getPreviousGroup(d));
    }

    @Test
    public void testGetLadderGroups() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");
        Group c = createGroup("c");
        Group d = createGroup("d");

        a.setLadder("ladder");
        b.setLadder("ladder");
        c.setLadder("ladder");
        d.setLadder("default");

        a.setRank(1000);
        b.setRank(100);
        c.setRank(10);
        d.setRank(1);

        pm.addGroup(a);
        pm.addGroup(b);
        pm.addGroup(c);
        pm.addGroup(d);

        List<Group> groups = pm.getLadderGroups("ladder");
        assertEquals(3, groups.size());
        assertTrue(groups.contains(a));
        assertTrue(groups.contains(b));
        assertTrue(groups.contains(c));
    }

    @Test
    public void testGetLadders() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");
        Group c = createGroup("c");
        Group d = createGroup("d");

        a.setLadder("ladder");
        b.setLadder("ladder");
        c.setLadder("ladder");
        d.setLadder("default");

        a.setRank(1000);
        b.setRank(100);
        c.setRank(10);
        d.setRank(1);

        pm.addGroup(a);
        pm.addGroup(b);
        pm.addGroup(c);
        pm.addGroup(d);

        List<String> ladders = pm.getLadders();
        assertEquals(2, ladders.size());
        assertTrue(ladders.contains("default"));
        assertTrue(ladders.contains("ladder"));
    }

    @Test
    public void testGetDefaultGroups() throws Exception
    {
        assertTrue(pm.getDefaultGroups().isEmpty());

        Group a = createGroup("a");
        Group b = createGroup("b");
        Group c = createGroup("c");
        Group d = createGroup("d");

        a.setIsdefault(true);
        b.setIsdefault(false);
        c.setIsdefault(true);
        d.setIsdefault(false);

        pm.addGroup(a);
        pm.addGroup(b);
        pm.addGroup(c);
        pm.addGroup(d);

        pm.addGroupInheritance(d, a);

        List<Group> defaultGroups = pm.getDefaultGroups();
        assertEquals(2, defaultGroups.size());
        assertTrue(defaultGroups.contains(a));
        assertTrue(defaultGroups.contains(c));
    }

    @Test
    public void testGetGroup() throws Exception
    {
        Group a = createGroup("a");

        pm.addGroup(a);

        assertEquals(pm.getGroup("a"), a);
        assertNull(pm.getGroup("b"));
    }

    @Test
    public void testGetUser() throws Exception
    {
        Sender player = platform.simulateUserJoin("TestPlayer");
        assertNotNull(pm.getUser(player.getName()));
        assertNotNull(pm.getUser(player.getUUID()));
        Sender player2 = platform.simulateUserJoin("TestPlayer2");
        assertEquals(player.getName(), pm.getUser(player.getName()).getName());
        assertEquals(player2.getName(), pm.getUser(player2.getName()).getName());

        pm.addUserPerm(pm.getUser(player.getName()), "some.permission");
        platform.simulateUserDisconnectJoin(player);
        assertTrue(pm.getUser(player.getName()).hasPerm("some.permission"));

        assertNull(pm.getUser((UUID)null));
        assertNull(pm.getUser((String)null));
    }

    @Test
    public void testCreateTempUser() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);
        b.setIsdefault(false);

        pm.addGroup(a);
        pm.addGroup(b);

        UUID uuid = UUID.randomUUID();
        String name = "Test";
        User tempUser = pm.createTempUser(name, uuid);

        assertEquals(name, tempUser.getName());
        assertEquals(uuid, tempUser.getUUID());

        assertEquals(1, tempUser.getGroups().size());
        assertTrue(tempUser.getGroups().contains(a));

        assertTrue(pm.getUsers().contains(tempUser));
        assertEquals(1, pm.getUsers().size());
    }

    @Test
    public void testGetGroups() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        assertEquals(2, pm.getGroups().size());
        assertTrue(pm.getGroups().contains(a));
        assertTrue(pm.getGroups().contains(b));
    }

    @Test
    public void testGetUsers() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        assertEquals(2, pm.getUsers().size());

        platform.simulateUserDisconnectJoin(player1);

        assertEquals(1, pm.getUsers().size());
    }

    @Test
    public void testGetRegisteredUsers() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        pm.setUserPrefix(pm.getUser(player1.getName()), "[a]", null, null);
        pm.setUserPrefix(pm.getUser(player2.getName()), "[b]", null, null);

        assertEquals(2, pm.getRegisteredUsers().size());

        platform.simulateUserDisconnectJoin(player1);

        assertEquals(2, pm.getRegisteredUsers().size());
    }

    @Test
    public void testGetGroupUsers() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.addUserGroup(pm.getUser(player2.getUUID()), a);
        pm.addUserGroup(pm.getUser(player1.getUUID()), b);

        assertEquals(2, pm.getGroupUsers(a).size());
        assertEquals(1, pm.getGroupUsers(b).size());
    }

    @Test
    public void testDeleteUser() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");

        pm.addUserPerm(pm.getUser(player1.getName()), "some.permission");

        assertTrue(pm.getUser(player1.getName()).hasPerm("some.permission"));

        pm.deleteUser(pm.getUser(player1.getName()));

        assertNull(pm.getUser(player1.getName()));
    }

    @Test
    public void testDeleteGroup() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        assertNotNull(pm.getGroup("a"));
        assertNotNull(pm.getGroup("b"));

        pm.deleteGroup(a);

        assertNull(pm.getGroup("a"));
        assertNotNull(pm.getGroup("b"));
    }

    @Test
    public void testAddUser() throws Exception
    {
        User user = new User("Test", UUID.randomUUID(), new ArrayList<Group>(), new ArrayList<String>(), new HashMap<String, Server>(), "", "", "");
        pm.addUser(user);
        assertEquals(user, pm.getUser("Test"));
    }

    @Test
    public void testAddGroup() throws Exception
    {
        Group a = createGroup("a");

        pm.addGroup(a);

        assertEquals(a, pm.getGroup("a"));

        assertTrue(pm.getGroups().contains(a));
    }

    @Test
    public void testFormat() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.addUserGroup(pm.getUser(player2.getUUID()), a);
        pm.addUserGroup(pm.getUser(player1.getUUID()), b);

        pm.setUserPrefix(pm.getUser(player2.getUUID()), "&a", null, null);

        pm.format();

        assertEquals(2, pm.getUser(player1.getUUID()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getUUID()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getUUID()).getPrefix());
    }

    @Test
    public void testCleanup() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.addUserGroup(pm.getUser(player2.getUUID()), a);
        pm.addUserGroup(pm.getUser(player1.getUUID()), b);

        pm.setUserPrefix(pm.getUser(player2.getUUID()), "&a", null, null);

        pm.cleanup();

        assertEquals(2, pm.getUser(player1.getUUID()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getUUID()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getUUID()).getPrefix());
    }

    @Test
    public void testAddUserGroup() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");

        Group a = createGroup("a");
        pm.addGroup(a);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);

        assertEquals(1, pm.getUser(player1.getUUID()).getGroups().size());
        assertEquals(a, pm.getUser(player1.getUUID()).getGroups().get(0));
    }

    @Test
    public void testRemoveUserGroup() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");

        Group a = createGroup("a");
        pm.addGroup(a);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.removeUserGroup(pm.getUser(player1.getUUID()), a);

        assertEquals(0, pm.getUser(player1.getUUID()).getGroups().size());

        pm.removeUserGroup(pm.getUser(player1.getUUID()), a);

        assertEquals(0, pm.getUser(player1.getUUID()).getGroups().size());
    }

    @Test
    public void testAddUserPerm() throws Exception
    {
        Sender player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerm(pm.getUser(player.getUUID()), "some.permission");

        assertTrue(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testRemoveUserPerm() throws Exception
    {
        Sender player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerm(pm.getUser(player.getUUID()), "some.permission");
        pm.removeUserPerm(pm.getUser(player.getUUID()), "some.permission");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testAddUserPerServerPerm() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerServerPerm(pm.getUser(player.getUUID()), "server", "some.permission");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.server = "server";

        assertTrue(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testRemoveUserPerServerPerm() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerServerPerm(pm.getUser(player.getUUID()), "server", "some.permission");
        pm.removeUserPerServerPerm(pm.getUser(player.getUUID()), "server", "some.permission");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.server = "server";

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testAddUserPerServerWorldPerm() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerServerWorldPerm(pm.getUser(player.getUUID()), "server", "world", "some.permission");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.server = "server";

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.world = "world";

        assertTrue(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.server = "server2";

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testRemoveUserPerServerWorldPerm() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        pm.addUserPerServerWorldPerm(pm.getUser(player.getUUID()), "server", "world", "some.permission");
        pm.removeUserPerServerWorldPerm(pm.getUser(player.getUUID()), "server", "world", "some.permission");

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));

        player.server = "server";
        player.world = "world";

        assertFalse(bungeePerms.getPermissionsChecker().hasPermOnServerInWorld(player, "some.permission"));
    }

    @Test
    public void testSetUserDisplay() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        pm.setUserDisplay(pm.getUser(player.getName()), "test", null, null);
        pm.setUserDisplay(pm.getUser(player.getName()), "test2", "server", null);
        pm.setUserDisplay(pm.getUser(player.getName()), "test3", "server2", "world");
        pm.setUserDisplay(pm.getUser(player.getName()), "test4", "server2", "world2");

        assertEquals("test", pm.getUser(player.getName()).getDisplay());
        assertEquals("test2", pm.getUser(player.getName()).getServer("server").getDisplay());
        assertEquals("test3", pm.getUser(player.getName()).getServer("server2").getWorld("world").getDisplay());
        assertEquals("test4", pm.getUser(player.getName()).getServer("server2").getWorld("world2").getDisplay());
    }

    @Test
    public void testSetUserPrefix() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        pm.setUserPrefix(pm.getUser(player.getName()), "test", null, null);
        pm.setUserPrefix(pm.getUser(player.getName()), "test2", "server", null);
        pm.setUserPrefix(pm.getUser(player.getName()), "test3", "server2", "world");
        pm.setUserPrefix(pm.getUser(player.getName()), "test4", "server2", "world2");

        assertEquals("test", pm.getUser(player.getName()).getPrefix());
        assertEquals("test2", pm.getUser(player.getName()).getServer("server").getPrefix());
        assertEquals("test3", pm.getUser(player.getName()).getServer("server2").getWorld("world").getPrefix());
        assertEquals("test4", pm.getUser(player.getName()).getServer("server2").getWorld("world2").getPrefix());
    }

    @Test
    public void testSetUserSuffix() throws Exception
    {
        MockPlayer player = platform.simulateUserJoin("TestPlayer");

        pm.setUserSuffix(pm.getUser(player.getName()), "test", null, null);
        pm.setUserSuffix(pm.getUser(player.getName()), "test2", "server", null);
        pm.setUserSuffix(pm.getUser(player.getName()), "test3", "server2", "world");
        pm.setUserSuffix(pm.getUser(player.getName()), "test4", "server2", "world2");

        assertEquals("test", pm.getUser(player.getName()).getSuffix());
        assertEquals("test2", pm.getUser(player.getName()).getServer("server").getSuffix());
        assertEquals("test3", pm.getUser(player.getName()).getServer("server2").getWorld("world").getSuffix());
        assertEquals("test4", pm.getUser(player.getName()).getServer("server2").getWorld("world2").getSuffix());
    }

    @Test
    public void testAddGroupPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerm(group, "some.permission");

        assertTrue(group.has("some.permission"));
    }

    @Test
    public void testRemoveGroupPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerm(group, "some.permission");
        pm.removeGroupPerm(group, "some.permission");

        assertFalse(group.has("some.permission"));
    }

    @Test
    public void testAddGroupPerServerPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerServerPerm(group, "server", "some.permission");

        assertFalse(group.has("some.permission"));
        assertTrue(group.hasOnServer("some.permission", "server"));
    }

    @Test
    public void testRemoveGroupPerServerPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerServerPerm(group, "server", "some.permission");
        pm.removeGroupPerServerPerm(group, "server", "some.permission");

        assertFalse(group.has("some.permission"));
        assertFalse(group.hasOnServer("some.permission", "server"));
    }

    @Test
    public void testAddGroupPerServerWorldPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerServerWorldPerm(group, "server", "world", "some.permission");

        assertFalse(group.has("some.permission"));
        assertFalse(group.hasOnServer("some.permission", "server"));
        assertTrue(group.hasOnServerInWorld("some.permission", "server", "world"));
    }

    @Test
    public void testRemoveGroupPerServerWorldPerm() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.addGroupPerServerWorldPerm(group, "server", "world", "some.permission");
        pm.removeGroupPerServerWorldPerm(group, "server", "world", "some.permission");

        assertFalse(group.has("some.permission"));
        assertFalse(group.hasOnServer("some.permission", "server"));
        assertFalse(group.hasOnServerInWorld("some.permission", "server", "world"));
    }

    @Test
    public void testAddGroupInheritance() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addGroupPerm(a, "some.permission");
        pm.addGroupInheritance(b, a);

        assertTrue(b.getInheritances().contains("a"));
        assertTrue(b.has("some.permission"));
    }

    @Test
    public void testRemoveGroupInheritance() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addGroupPerm(a, "some.permission");
        pm.addGroupInheritance(b, a);
        pm.removeGroupInheritance(b, a);

        assertFalse(b.getInheritances().contains("a"));
        assertFalse(b.has("some.permission"));
    }

    @Test
    public void testLadderGroup() throws Exception
    {
        Group a = createGroup("a");

        pm.addGroup(a);

        pm.ladderGroup(a, "ladder");

        assertEquals("ladder", a.getLadder());
    }

    @Test
    public void testRankGroup() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.rankGroup(group, 100);

        assertEquals(100, group.getRank());
    }

    @Test
    public void testWeightGroup() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.weightGroup(group, 100);

        assertEquals(100, group.getWeight());
    }

    @Test
    public void testSetGroupDefault() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.setGroupDefault(group, true);

        assertTrue(group.isDefault());

        pm.setGroupDefault(group, false);

        assertFalse(group.isDefault());
    }

    @Test
    public void testSetGroupDisplay() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.setGroupDisplay(group, "test", null, null);
        pm.setGroupDisplay(group, "test2", "server", null);
        pm.setGroupDisplay(group, "test3", "server2", "world");
        pm.setGroupDisplay(group, "test4", "server2", "world2");

        assertEquals("test", group.getDisplay());
        assertEquals("test2", group.getServer("server").getDisplay());
        assertEquals("test3", group.getServer("server2").getWorld("world").getDisplay());
        assertEquals("test4", group.getServer("server2").getWorld("world2").getDisplay());
    }

    @Test
    public void testSetGroupPrefix() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.setGroupPrefix(group, "test", null, null);
        pm.setGroupPrefix(group, "test2", "server", null);
        pm.setGroupPrefix(group, "test3", "server2", "world");
        pm.setGroupPrefix(group, "test4", "server2", "world2");

        assertEquals("test", group.getPrefix());
        assertEquals("test2", group.getServer("server").getPrefix());
        assertEquals("test3", group.getServer("server2").getWorld("world").getPrefix());
        assertEquals("test4", group.getServer("server2").getWorld("world2").getPrefix());
    }

    @Test
    public void testSetGroupSuffix() throws Exception
    {
        Group group = createGroup("group");

        pm.addGroup(group);

        pm.setGroupSuffix(group, "test", null, null);
        pm.setGroupSuffix(group, "test2", "server", null);
        pm.setGroupSuffix(group, "test3", "server2", "world");
        pm.setGroupSuffix(group, "test4", "server2", "world2");

        assertEquals("test", group.getSuffix());
        assertEquals("test2", group.getServer("server").getSuffix());
        assertEquals("test3", group.getServer("server2").getWorld("world").getSuffix());
        assertEquals("test4", group.getServer("server2").getWorld("world2").getSuffix());
    }

    @Test
    public void testMigrateBackEnd() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.addUserGroup(pm.getUser(player2.getUUID()), a);
        pm.addUserGroup(pm.getUser(player1.getUUID()), b);

        pm.setUserPrefix(pm.getUser(player2.getUUID()), "&a", null, null);

        pm.migrateBackEnd(BackEndType.YAML);

        assertEquals(2, pm.getUser(player1.getUUID()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getUUID()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getUUID()).getPrefix());
    }

    @Test
    public void testReloadUser() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getUUID()), a);
        pm.addUserGroup(pm.getUser(player2.getUUID()), a);
        pm.addUserGroup(pm.getUser(player1.getUUID()), b);

        pm.setUserPrefix(pm.getUser(player2.getUUID()), "&a", null, null);

        pm.reloadUser(player1.getName());

        assertEquals(2, pm.getUser(player1.getName()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getName()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getName()).getPrefix());
    }

    @Test
    public void testReloadGroup() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getName()), a);
        pm.addUserGroup(pm.getUser(player2.getName()), a);
        pm.addUserGroup(pm.getUser(player1.getName()), b);

        pm.setUserPrefix(pm.getUser(player2.getName()), "&a", null, null);

        pm.reloadGroup("a");

        assertEquals(2, pm.getUser(player1.getName()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getName()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getName()).getPrefix());
    }

    @Test
    public void testReloadUsers() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getName()), a);
        pm.addUserGroup(pm.getUser(player2.getName()), a);
        pm.addUserGroup(pm.getUser(player1.getName()), b);

        pm.setUserPrefix(pm.getUser(player2.getName()), "&a", null, null);

        pm.reloadUsers();

        assertEquals(2, pm.getUser(player1.getName()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getName()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getName()).getPrefix());
    }

    @Test
    public void testReloadGroups() throws Exception
    {
        Sender player1 = platform.simulateUserJoin("Player1");
        Sender player2 = platform.simulateUserJoin("Player2");

        Group a = createGroup("a");
        Group b = createGroup("b");

        a.setIsdefault(true);

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addUserGroup(pm.getUser(player1.getName()), a);
        pm.addUserGroup(pm.getUser(player2.getName()), a);
        pm.addUserGroup(pm.getUser(player1.getName()), b);

        pm.setUserPrefix(pm.getUser(player2.getName()), "&a", null, null);

        pm.reloadGroups();

        assertEquals(2, pm.getUser(player1.getName()).getGroups().size());
        assertEquals(1, pm.getUser(player2.getName()).getGroups().size());
        assertEquals("&a", pm.getUser(player2.getName()).getPrefix());
    }

    @Test
    public void testGetBackEnd() throws Exception
    {
        assertEquals(BackEndType.YAML, pm.getBackEnd().getType());
    }

    @Test
    public void testPerServerPermissionInheritance() throws Exception
    {
        Group a = createGroup("a");
        Group b = createGroup("b");

        pm.addGroup(a);
        pm.addGroup(b);

        pm.addGroupInheritance(b, a);

        MockPlayer player1 = platform.simulateUserJoin("Player1");
        player1.server = "server";

        pm.addUserGroup(pm.getUser(player1.getName()), b);

        assertFalse(BungeePerms.getInstance().getPermissionsChecker().hasPermOnServerInWorld(player1, "some.permission"));

        pm.addGroupPerServerPerm(a, "server", "some.permission");

        assertTrue(BungeePerms.getInstance().getPermissionsChecker().hasPermOnServerInWorld(player1, "some.permission"));
    }

    private Group createGroup(String name)
    {
        return new Group(name, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, Server>(), 0, 20, "default", false, "", "", "");
    }

}