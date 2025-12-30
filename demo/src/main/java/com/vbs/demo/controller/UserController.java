package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class UserController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    HistoryRepo historyRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        History h1 = new History();
        h1.setDescription("User self created "+user.getUsername());
        historyRepo.save(h1);
        return "Signup Successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto v)
    {
        User user = userRepo.findByUsername(v.getUsername());
        if(user == null){
            return "User Not Found";
        }
        if(!v.getPassword().equals(user.getPassword())){
            return "Password Incorrect";
        }
        if(!v.getRole().equals(user.getRole())){
            return "Role Incorrect";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto details(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User Not Found"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());

        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj){
        User user = userRepo.findById(obj.getId()).orElseThrow(()-> new RuntimeException("User Not Found"));
        History h1 = new History();

        if(obj.getKey().equalsIgnoreCase("name")){
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            h1.setDescription("User changed name from "+user.getName()+" to "+obj.getValue());
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password")){
            if(user.getPassword().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            user.setPassword(obj.getValue());
            h1.setDescription("Password changed by user: "+user.getUsername());
        }
        else if(obj.getKey().equalsIgnoreCase("email")){
            if(user.getEmail().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2 != null) return "Email Already Exists";
            user.setEmail(obj.getValue());
            h1.setDescription("User changed email from "+user.getEmail()+" to "+obj.getValue());
        }
        historyRepo.save(h1);
        userRepo.save(user);
        return "Update Done Successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User add_user,@PathVariable int adminId){
        History h1 = new History();
        h1.setDescription("User "+add_user.getUsername()+" created by admin "+adminId);
        historyRepo.save(h1);
        userRepo.save(add_user);
        return "Successfully added";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order){
        Sort sort;
        if(order.equalsIgnoreCase("desc")){
            sort = Sort.by(sortBy).descending();
        }
        else{
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("Customer",sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword){
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"Customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUsers(@PathVariable int userId,@PathVariable int adminId){
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        History h1 = new History();
        h1.setDescription("User "+user.getUsername()+" deleted by admin "+adminId);
        if(user.getBalance() > 0){
            return "Balance should be zero";
        }

        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully";
    }
}
