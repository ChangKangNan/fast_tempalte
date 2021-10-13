package com.service;

import com.entity.User;

import java.util.List;

/**
 * @author kangnan.chang
 */
public interface UserService {

	 User getUserById(long id);

	 User fetchUserByEmail(String email);

	 User getUserByEmail(String email);

	 String getNameByEmail(String email);

	 List getUsers(int pageIndex) ;

	 User login(String email, String password);

	 User register(String email, String password, String name,Long departmentId);

	 void updateUser(Long id, String name);

	 void deleteUser(Long id);

	 void selectByCondition();

}
