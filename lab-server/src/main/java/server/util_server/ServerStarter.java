package server.util_server;

import com.thoughtworks.xstream.converters.ConversionException;
import common.util_common.Request;
import common.util_common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.file_Handlers.FileReader;
import server.client_commands.*;
import server.file_Handlers.FileWriter;
import server.server_commands.*;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStarter.class);
    private final Scanner scanner = new Scanner(System.in);
    private final int maxPort = 65535;
    private ServerSocket serverSocket;
    private final String fileName;
    private final ServerCommandReader serverCommandReader = new ServerCommandReader(scanner);
    private CollectionManager collection;
    public static CommandManager commandManager;
    private final FileReader fileReader = new FileReader();
    private final FileWriter fileWriter = new FileWriter();

    public ServerStarter(String fileName) {
        this.fileName = fileName;
    }

    public void startServer() {
        try {
            collection = fileReader.readFile(this.fileName);
            LOGGER.info("File " + this.fileName + " was loaded!");
            commandManager = new CommandManager(new Show(collection),
                    new Exit(),
                    new Help(ServerConfig.getClientAvailableCommands()),
                    new Info(collection),
                    new Clear(collection),
                    new Add(collection),
                    new RemoveByID(collection),
                    new UpdateByID(collection),
                    new ExecuteScript(),
                    new AddIfMin(collection),
                    new RemoveLower(collection),
                    new FilterLessThanMaximumPoint(collection),
                    new FilterGreaterThanDifficulty(collection),
                    new PrintFieldDescendingDiscipline(collection),
                    new Head(collection),
                    new ServerExit(scanner, fileWriter, collection),
                    new ServerSave(collection, fileWriter));
            inputPort();
            LOGGER.info("Server is running now!");
            System.out.println("Welcome to the server!");
            ServerConsole serverConsole = new ServerConsole(serverCommandReader, commandManager);
            serverConsole.start();
            while (ServerConfig.getRunning()) {
                try {
                    Request acceptedRequest = serverSocket.listenForRequest();
                    if (acceptedRequest != null) {
                        LOGGER.info("Request was accepted!");
                        Response responseToSend = commandManager.executeClientCommand(acceptedRequest);
                        LOGGER.info("Client command \"" + acceptedRequest.getCommandName() + "\" was executed!");
                        serverSocket.sendResponse(responseToSend);
                        LOGGER.info("Response to client was send!");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("An error occurred while deserializing the request, try again");
                }
            }
            try {
                serverSocket.stopServer();
                LOGGER.info("Server was stopped!");
            } catch (IOException e) {
                System.out.println("An error occurred during stopping the server");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ConversionException e) {
            System.out.println("Error during type conversion");
            System.exit(1);
        }
    }

    private void inputPort() throws IOException {
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
