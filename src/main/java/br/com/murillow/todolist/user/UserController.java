package br.com.murillow.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel userModel) {
        var userExists = this.userRepository.findByUsername(userModel.getUsername());

        if(userExists != null) {
            //Status Code + Error Message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( "Username is being used already.");
        }

        var hashPassword = BCrypt.withDefaults()
            .hashToString(12, userModel.getPassword().toCharArray());

        userModel.setPassword(hashPassword);

        var userCreated = this.userRepository.save(userModel);
        return ResponseEntity.status(HttpStatus.OK).body(userCreated);
    }
}
