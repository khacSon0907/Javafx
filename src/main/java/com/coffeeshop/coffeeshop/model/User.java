package com.coffeeshop.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int UserId;
    private String userName;
    private String password;
    private Role role;


}
