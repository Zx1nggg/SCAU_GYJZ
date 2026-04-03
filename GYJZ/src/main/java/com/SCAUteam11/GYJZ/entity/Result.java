package com.SCAUteam11.GYJZ.entity;

import java.util.HashMap;

public class Result extends HashMap<String, Object> {
    public Result() {
    }
    public Result(Integer code, String msg, Object data) {
        this.put("code", code);
        this.put("msg", msg);
        this.put("data", data);
    }


    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }
    public static Result success() {
        return new Result(200, "操作成功", null);
    }

    public static Result fail(String msg) {
        return new Result(500, msg, null);
    }

    public static Result fail(String msg, Object data) {
        return new Result(500, msg, data);
    }

}

