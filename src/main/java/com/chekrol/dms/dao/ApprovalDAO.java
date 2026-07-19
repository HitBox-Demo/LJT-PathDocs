package com.chekrol.dms.dao;

import com.chekrol.dms.model.ApprovalRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApprovalDAO {
    public void decide(User boss,long documentId,String decision,String remarks,Long newDepartmentId)throws SQLException{
        String lock="SELECT created_by,destination_department_id,document_code FROM dms_document WHERE document_id=? AND boss_id=? AND status='PENDING_APPROVAL' FOR UPDATE";
        String updateApprove="UPDATE dms_document SET status='ROUTED',destination_department_id=?,approved_at=SYSTIMESTAMP,updated_at=SYSTIMESTAMP WHERE document_id=?";
        String updateReject="UPDATE dms_document SET status='RETURNED_FOR_CORRECTION',rejection_reason=?,updated_at=SYSTIMESTAMP WHERE document_id=?";
        String insert="INSERT INTO dms_approval(document_id,approver_id,decision,remarks,previous_department_id,new_department_id) VALUES(?,?,?,?,?,?)";
        String history="INSERT INTO dms_document_history(document_id,user_id,action,old_value,new_value,remarks) VALUES(?,?,?,'PENDING_APPROVAL',?,?)";
        try(Connection c=DatabaseConnection.getConnection()){c.setAutoCommit(false);try{
            long creator;long oldDept;String code;
            try(PreparedStatement ps=c.prepareStatement(lock)){ps.setLong(1,documentId);ps.setLong(2,boss.getId());try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Document is not pending for this boss.");creator=rs.getLong(1);oldDept=rs.getLong(2);code=rs.getString(3);}}
            long finalDept=newDepartmentId==null?oldDept:newDepartmentId;
            if("APPROVE".equals(decision)){try(PreparedStatement ps=c.prepareStatement(updateApprove)){ps.setLong(1,finalDept);ps.setLong(2,documentId);ps.executeUpdate();}}
            else if("REJECT".equals(decision)){if(remarks==null||remarks.isBlank())throw new SQLException("A rejection reason is required.");try(PreparedStatement ps=c.prepareStatement(updateReject)){ps.setString(1,remarks);ps.setLong(2,documentId);ps.executeUpdate();}}
            else throw new SQLException("Unsupported decision.");
            try(PreparedStatement ps=c.prepareStatement(insert)){ps.setLong(1,documentId);ps.setLong(2,boss.getId());ps.setString(3,decision);ps.setString(4,remarks);ps.setLong(5,oldDept);ps.setLong(6,finalDept);ps.executeUpdate();}
            try(PreparedStatement ps=c.prepareStatement(history)){ps.setLong(1,documentId);ps.setLong(2,boss.getId());ps.setString(3,decision);ps.setString(4,"APPROVE".equals(decision)?"ROUTED":"RETURNED_FOR_CORRECTION");ps.setString(5,remarks);ps.executeUpdate();}
            DocumentDAO.insertNotification(c,creator,"APPROVE".equals(decision)?"APPROVED":"RETURNED",code+("APPROVE".equals(decision)?" was approved.":" was returned for correction."),documentId);
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}}
    }

    public List<ApprovalRecord> listHistory(User user)throws SQLException{
        String sql="""
        SELECT a.approval_id,a.document_id,d.document_code,d.title,a.approver_id,u.full_name,a.decision,a.remarks,
               oldd.department_name old_department,newd.department_name new_department,a.decision_at
        FROM dms_approval a JOIN dms_document d ON d.document_id=a.document_id JOIN dms_user u ON u.user_id=a.approver_id
        LEFT JOIN dms_department oldd ON oldd.department_id=a.previous_department_id
        LEFT JOIN dms_department newd ON newd.department_id=a.new_department_id
        WHERE (?=1 OR a.approver_id=?) ORDER BY a.decision_at DESC
        """;
        List<ApprovalRecord> list=new ArrayList<>();boolean admin=user.hasRole("CLERK")||user.hasRole("SYSTEM_ADMIN");
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,admin?1:0);ps.setLong(2,user.getId());try(ResultSet rs=ps.executeQuery()){while(rs.next()){ApprovalRecord a=new ApprovalRecord();a.setId(rs.getLong(1));a.setDocumentId(rs.getLong(2));a.setDocumentCode(rs.getString(3));a.setDocumentTitle(rs.getString(4));a.setApproverId(rs.getLong(5));a.setApproverName(rs.getString(6));a.setDecision(rs.getString(7));a.setRemarks(rs.getString(8));a.setPreviousDepartment(rs.getString(9));a.setFinalDepartment(rs.getString(10));a.setDecisionAt(rs.getTimestamp(11).toLocalDateTime());list.add(a);}}}return list;
    }
}
