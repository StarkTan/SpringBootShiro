package com.stark.shiro;

import com.stark.entity.Permission;
import com.stark.entity.Role;
import com.stark.entity.User;
import com.stark.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class MyShiroRealm extends AuthorizingRealm {

    /**
     * 用于判断登录信息的方法
     */
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authcToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        String name = token.getUsername();
        String password = String.valueOf(token.getPassword());
        User user = UserService.getUserByName(name);
        if (null == user) {
            throw new UnknownAccountException();
        } else if (user.isLocked()) {
            throw new LockedAccountException();
        } else {
            String credentials = new Sha512Hash(password, user.getSalt()).toString();
            token.setPassword(credentials.toCharArray());
            return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
        }
    }

    /**
     * 这里是用来返回用户权限的方法
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        User user = (User) principalCollection.getPrimaryPrincipal();
        for (Role role : user.getRoles()) {
            info.addRole(role.getName());
            for (Permission permission : role.getPermissions()) {
                info.addStringPermission(permission.getName());
            }
        }
        return info;
    }
}
