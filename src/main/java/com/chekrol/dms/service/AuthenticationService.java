package com.chekrol.dms.service;

import com.chekrol.dms.dao.UserDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import com.chekrol.dms.util.PasswordUtil;

import java.sql.SQLException;

public class AuthenticationService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) throws SQLException {
        if (AppConfig.isDemoMode()) {
            return DemoData.authenticate(username, password);
        }

        User user = userDAO.findByUsername(username);
        if (user == null
                || !"ACTIVE".equals(user.getStatus())
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }
}
