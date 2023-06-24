package server.util_server;

import common.util_common.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;

public class RequestThread extends Thread{
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestThread.class);
    private final ServerSocket serverSocket;
    private final CommandManager commandManager;
    private final UserManager userManager;
    private final ExecutorService fixedService = Executors.newFixedThreadPool(5);
    private final ExecutorService cachedService = Executors.newCachedThreadPool();
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    public RequestThread(ServerSocket serverSocket, CommandManager commandManager, UserManager userManager) {
        this.serverSocket = serverSocket;
        this.commandManager = commandManager;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        while (ServerConfig.getRunning()) {
            try {
                Future<AddressRequest> listenFuture = fixedService.submit(serverSocket::listenForRequest);
                AddressRequest acceptedRequest = listenFuture.get();
                if (acceptedRequest != null) {
                    CompletableFuture.supplyAsync(acceptedRequest::getRequest)
                            .thenApplyAsync(request -> {
                                if (request.getRequestType().equals(RequestType.COMMAND)) {
                                    LOGGER.info("Client command \"" + request.getCommandName() + "\" was executed by " + request.getUsername());
                                    return commandManager.executeClientCommand(request);
                                } else if (request.getRequestType().equals(RequestType.REGISTER)) {
                                    LOGGER.info("User " + request.getUsername() + " was registered!");
                                    return userManager.registerUser(request);
                                } else {
                                    LOGGER.info("User " + request.getUsername() + " was authorised!");
                                    return userManager.loginUser(request);
                                }
                            }, cachedService)
                            .thenAcceptAsync(responseToSend -> {
                                try {
                                    serverSocket.sendResponse(responseToSend, acceptedRequest.getSocketAddress());
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }, forkJoinPool);
                }
            } catch (ExecutionException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("An error occurred while deserializing the request, try again");
            }
        }
        try {
            serverSocket.stopServer();
            fixedService.shutdown();
            cachedService.shutdown();
            forkJoinPool.shutdown();
            LOGGER.info("Server was stopped!");
        } catch (IOException e) {
            System.out.println("An error occurred during stopping the server");
        }
    }
}
