package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.platform.*;
import net.alpenblock.bungeeperms.platform.EventListener;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class AbstractTest
{
    MockPlatform platform = null;
    MockNetworkNotifier networkNotifier = null;
    BPConfig bpConfig = null;
    BungeePerms bungeePerms = null;
    PermissionsManager pm = null;

    @Before
    public void setUp() throws Exception
    {
        new File("tmp").mkdir();
        platform = new MockPlatform();
        Config config = new Config("tmp/config.yml");
        config.load();
        bpConfig = new BPConfig(config);
        bpConfig.load();
        networkNotifier = new MockNetworkNotifier();
        bungeePerms = new BungeePerms(platform, bpConfig, null, networkNotifier, new MockEventListener());
        bungeePerms.load();
        bungeePerms.enable();
        pm = BungeePerms.getInstance().getPermissionsManager();
        pm.getBackEnd().clearDatabase();
        pm.reload();
    }

    @After
    public void tearDown() throws Exception
    {
        platform = null;
        pm = null;
        bpConfig = null;
        bungeePerms = null;
        networkNotifier = null;
        File tmp = new File("tmp");
        delete(tmp);
    }

    private void delete(File file)
    {
        if (file.exists() && file.isDirectory()) {
            for (File file2 : file.listFiles())
            {
                delete(file2);
            }
        }
        file.delete();
    }

    protected class MockPlatform implements PlatformPlugin
    {
        public List<Sender> players = new ArrayList<>();
        public Map<Integer, Runnable> registeredRepeatingTasks = new HashMap<>();
        private int taskId = 1;

        @Override
        public String getPluginName()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getVersion()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthor()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPluginFolderPath()
        {
            return "tmp";
        }

        @Override
        public File getPluginFolder()
        {
            return new File("tmp");
        }

        @Override
        public Sender getPlayer(String name)
        {
            for (Sender player : players)
            {
                if (Objects.equals(player.getName(), name)) {
                    return player;
                }
            }
            return null;
        }

        @Override
        public Sender getPlayer(UUID uuid)
        {
            for (Sender player : players)
            {
                if (Objects.equals(player.getUUID(), uuid)) {
                    return player;
                }
            }
            return null;
        }

        @Override
        public Sender getConsole()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Sender> getPlayers()
        {
            return players;
        }

        @Override
        public Logger getLogger()
        {
            return Logger.getGlobal();
        }

        @Override
        public PlatformType getPlatformType()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isChatApiPresent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public MessageEncoder newMessageEncoder()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int registerRepeatingTask(Runnable r, long delay, long interval)
        {
            int id = taskId++;
            registeredRepeatingTasks.put(id, r);
            return id;
        }

        @Override
        public void cancelTask(int id)
        {
            registeredRepeatingTasks.remove(id);
        }

        public MockPlayer simulateUserJoin(String name) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            MockPlayer player = new MockPlayer(name, uuid);
            players.add(player);
            User u = bpConfig.isUseUUIDs() ? pm.getUser(uuid) : pm.getUser(name);
            if (u == null)
            {
                //create user and add default groups
                u = pm.createTempUser(name, uuid);
                pm.getBackEnd().saveUser(u, true);
            }
            return player;
        }

        public void simulateUserDisconnectJoin(Sender sender) {
            User u = bpConfig.isUseUUIDs() ? pm.getUser(sender.getUUID()) : pm.getUser(sender.getName());
            pm.removeUserFromCache(u);
        }
    }

    protected class MockEventListener implements EventListener {
        private boolean isEnabled = false;

        @Override
        public void enable()
        {
            isEnabled = true;
        }

        @Override
        public void disable()
        {
            isEnabled = false;
        }
    }

    protected class MockPlayer implements Sender {
        private String name;
        private UUID uuid;
        public String server = null;
        public String world = null;

        public MockPlayer(String name, UUID uuid)
        {
            this.name = name;
            this.uuid = uuid;
        }

        @Override
        public void sendMessage(String message)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendMessage(MessageEncoder encoder)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public UUID getUUID()
        {
            return uuid;
        }

        @Override
        public String getServer()
        {
            return server;
        }

        @Override
        public String getWorld()
        {
            return world;
        }

        @Override
        public boolean isConsole()
        {
            return false;
        }

        @Override
        public boolean isPlayer()
        {
            return true;
        }

        @Override
        public boolean isOperator()
        {
            return false;
        }
    }

    protected class MockNetworkNotifier implements NetworkNotifier {

        @Override
        public void deleteUser(User u, String origin)
        {

        }

        @Override
        public void deleteGroup(Group g, String origin)
        {

        }

        @Override
        public void reloadUser(User u, String origin)
        {

        }

        @Override
        public void reloadGroup(Group g, String origin)
        {

        }

        @Override
        public void reloadUsers(String origin)
        {

        }

        @Override
        public void reloadGroups(String origin)
        {

        }

        @Override
        public void reloadAll(String origin)
        {

        }
    }
}
