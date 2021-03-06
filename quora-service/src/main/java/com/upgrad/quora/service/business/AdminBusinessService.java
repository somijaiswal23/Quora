package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    /**
     * This method helps create new user
     *
     * @param userEntity the UserEntity object from which new user will be created
     *
     * @return UserEntity object
     *
     * @throws SignUpRestrictedException if validation for user details conflicts
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {

        // Validate if requested user name is available
        UserEntity existingUser = userDao.getUserByUserName(userEntity.getUserName());
        if (existingUser != null) {
            throw new SignUpRestrictedException("SGR-1", "Try any other Username, this Username has already been taken.");
        }

        // Validate if provided email id is available
        existingUser = userDao.getUserByEmail(userEntity.getEmail());
        if (existingUser != null) {
            throw new SignUpRestrictedException("SGR-2", "This user has already been registered, try with any other emailID");
        }

        // Assign default password if password is not provided
        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("quora-123");
        }

        // Encrypt salt and password
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        return userDao.createUser(userEntity);
    }
}
