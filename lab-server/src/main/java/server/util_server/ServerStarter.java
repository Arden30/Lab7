package server.util_server;

import com.thoughtworks.xstream.converters.ConversionException;
import common.util_common.Request;
import common.util_common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.data_base.DataBaseConnection;
import server.data_base.DataBaseInitialization;
import server.data_base.DataBaseManager;
import server.file_Handlers.FileReader;
import server.client_commands.*;
import server.file_Handlers.FileWriter;
import server.server_commands.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStarter.class);
    private final Scanner scanner = new Scanner(System.in);
    private ServerSocket serverSocket;
    private final ServerCommandReader serverCommandReader = new ServerCommandReader(scanner);
    private CollectionManager collection;
    private CommandManager commandManager;
    private DataBaseManager dataBaseManager;

    private UserManager userManager;

    private final FileReader fileReader = new FileReader();
    private final FileWriter fileWriter = new FileWriter();
    private Connection dbConnection;

    public void startServer() {
        try {
            collection = new CollectionManager();
            DataBaseConnection dataBaseConnection = new DataBaseConnection();
            Connection dbConnection = dataBaseConnection.connect();
            DataBaseInitialization dataBaseInitialization = new DataBaseInitialization(dbConnection);
            dataBaseInitialization.initializeDB();
            collection.setLabWorks(dataBaseInitialization.fillCollection(dbConnection));
            dataBaseManager = new DataBaseManager(dbConnection);
            commandManager = new CommandManager(new Show(collection),
                    new Exit(),
                    new Help(ServerConfig.getClientAvailableCommands()),
                    new Info(collection),
                    new Clear(collection, dataBaseManager),
                    new Add(collection, dataBaseManager),
                    new RemoveByID(collection, dataBaseManager),
                    new UpdateByID(collection, dataBaseManager),
                    new ExecuteScript(),
                    new AddIfMin(collection, dataBaseManager),
                    new RemoveLower(collection, dataBaseManager),
                    new FilterLessThanMaximumPoint(collection),
                    new FilterGreaterThanDifficulty(collection),
                    new PrintFieldDescendingDiscipline(collection),
                    new Head(collection),
                    new ServerExit(scanner, collection));
            inputPort();
            LOGGER.info("Server is running now!");
            System.out.println("Welcome to the server!");
            userManager = new UserManager(dataBaseManager);
            ServerThread serverThread = new ServerThread(serverCommandReader, commandManager);
            RequestThread requestThread = new RequestThread(serverSocket, commandManager, userManager);
            serverThread.start();
            requestThread.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ConversionException e) {
            System.out.println("Error during type conversion");
            System.exit(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            //System.out.println("Problems during SQL DB initialization");
        }
    }

    private void inputPort() throws IOException {
        int maxPort = 65535;
        System.out.println("Do you want to use a default port? [y/n]");
        try {
            String s = scanner.nextLine().trim().toLowerCase();
            if ("n".equals(s)) {
                System.out.println("Please enter the remote host port (1-65535)");
                String port = scanner.nextLine();
                try {
                    int portInt = Integer.parseInt(port);
                    if (portInt > 0 && portInt <= maxPort) {
                        serverSocket = new ServerSocket(portInt);
                    } else {
                        System.out.println("The number did not fall within the limits, repeat the input");
                        inputPort();
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error processing the number, repeat the input");
                    inputPort();
                }
            } else if ("y".equals(s)) {
                serverSocket = new ServerSocket();
            } else {
                System.out.println("You entered not valid symbol, try again");
                inputPort();
            }
        } catch (NoSuchElementException e) {
            System.out.println("An invalid character has been entered, forced shutdown!");
            System.exit(1);
        }
    }
}
