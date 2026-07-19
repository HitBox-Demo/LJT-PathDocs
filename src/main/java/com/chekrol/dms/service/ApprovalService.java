package com.chekrol.dms.service;

import com.chekrol.dms.dao.ApprovalDAO;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DemoData;
import java.sql.SQLException;

public class ApprovalService {
    private final ApprovalDAO dao=new ApprovalDAO();
    public void decide(User boss,long documentId,String decision,String remarks,Long newDepartmentId)throws SQLException{
        if(AppConfig.isDemoMode()){DemoData.action(boss,documentId,decision,remarks,newDepartmentId);return;}
        dao.decide(boss,documentId,decision,remarks,newDepartmentId);
    }
}
