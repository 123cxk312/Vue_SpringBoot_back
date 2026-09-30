package com.example.demo.enums;

import java.util.Locale;

public enum RoleEnum {
    STUDENT("STUDENT"),
    TEACHER("TEACHER"),
    ADMIN("ADMIN");
    private final String code;

    RoleEnum(String code) {
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public static RoleEnum fromCode(String code){
        if(code==null||code.isBlank()){
            return null;
        }
        try{
            return RoleEnum.valueOf(code.trim().toUpperCase(Locale.ROOT));
        }catch (Exception e){
            return null;
        }
    }
}
