package com.chekrol.dms.model;

import java.io.Serializable;

public class Department implements Serializable {
    private long id;
    private String code;
    private String name;
    private String status;

    public Department() {}
    public Department(long id, String code, String name, String status) {
        this.id = id; this.code = code; this.name = name; this.status = status;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
