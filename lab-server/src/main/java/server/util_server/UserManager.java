package server.util_server;

import common.util_common.Request;
import common.util_common.Response;
import server.data_base.DataBaseManager;

public class UserManager {
    private final DataBaseManager dataBaseManager;
    public UserManager(DataBaseManager dataBaseManager) {
        this.dataBaseManager = dataBaseManager;
    }

    public Response registerUser(Request request) {
        if (!dataBaseManager.checkUserExist(request.getUsername())) {
            dataBaseManager.addUser(request.getUsername(), request.getPassword());
            return new Response("Registration was completed successfully!");
        } else {
            return new Response("User with such login already exists!", false);
        }
    }

    public Response loginUser(Request request) {
        if (dataBaseManager.validateUser(request.getUsername(), request.getPassword())) {
            return new Response("Login successfully!");
        } else {
            return new Response("Wrong login or password!", false);
        }
    }
}
