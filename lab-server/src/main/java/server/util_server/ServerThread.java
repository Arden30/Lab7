package server.util_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerThread extends Thread{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerThread.class);
    private final ServerCommandReader serverCommandListener;
    private final CommandManager commandManager;

    public ServerThread(ServerCommandReader serverCommandListener, CommandManager commandManager) {
        this.serverCommandListener = serverCommandListener;
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        while (ServerConfig.getRunning()) {
            String command = serverCommandListener.readCommand();
            System.out.println(commandManager.executeServerCommand(command));
        }
    }
}
