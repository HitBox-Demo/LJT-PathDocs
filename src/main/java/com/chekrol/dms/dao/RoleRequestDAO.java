package com.chekrol.dms.dao;

import com.chekrol.dms.model.RoleRequest;
import com.chekrol.dms.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleRequestDAO {
    public List<RoleRequest> listPending()throws SQLException{
        String sql="""
        SELECT rr.request_id,rr.user_id,u.full_name,rr.current_role_code,rr.requested_role_code,rr.requested_by,rb.full_name,rr.status,rr.requested_at,rr.remarks
        FROM dms_role_change_request rr JOIN dms_user u ON u.user_id=rr.user_id JOIN dms_user rb ON rb.user_id=rr.requested_by
        WHERE rr.status='PENDING' ORDER BY rr.requested_at
        """;List<RoleRequest> list=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){while(rs.next()){RoleRequest r=new RoleRequest();r.setId(rs.getLong(1));r.setUserId(rs.getLong(2));r.setUserName(rs.getString(3));r.setCurrentRole(rs.getString(4));r.setRequestedRole(rs.getString(5));r.setRequestedById(rs.getLong(6));r.setRequestedByName(rs.getString(7));r.setStatus(rs.getString(8));r.setRequestedAt(rs.getTimestamp(9).toLocalDateTime());r.setRemarks(rs.getString(10));list.add(r);}}return list;
    }

    public void create(long userId,String currentRole,String requestedRole,long requestedBy,String remarks)throws SQLException{
        String sql="INSERT INTO dms_role_change_request(user_id,current_role_code,requested_role_code,requested_by,status,remarks) VALUES(?,?,?,?,'PENDING',?)";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,userId);ps.setString(2,currentRole);ps.setString(3,requestedRole);ps.setLong(4,requestedBy);ps.setString(5,remarks);ps.executeUpdate();}
    }

    public void decide(long requestId,long bossId,String decision,String remarks)throws SQLException{
        if(!"APPROVE".equals(decision)&&!"REJECT".equals(decision))throw new SQLException("Unsupported role decision.");
        String lock="SELECT user_id,current_role_code,requested_role_code,requested_by FROM dms_role_change_request WHERE request_id=? AND status='PENDING' FOR UPDATE";
        String roleId="SELECT role_id FROM dms_role WHERE role_code=?";
        try(Connection c=DatabaseConnection.getConnection()){c.setAutoCommit(false);try{
            long userId,requester;String current,requested;
            try(PreparedStatement ps=c.prepareStatement(lock)){ps.setLong(1,requestId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Role request is no longer pending.");userId=rs.getLong(1);current=rs.getString(2);requested=rs.getString(3);requester=rs.getLong(4);}}
            if("APPROVE".equals(decision)){
                if(current!=null&&!current.isBlank()){try(PreparedStatement ps=c.prepareStatement("DELETE FROM dms_user_role WHERE user_id=? AND role_id=(SELECT role_id FROM dms_role WHERE role_code=?)")){ps.setLong(1,userId);ps.setString(2,current);ps.executeUpdate();}}
                long rid;try(PreparedStatement ps=c.prepareStatement(roleId)){ps.setString(1,requested);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Requested role does not exist.");rid=rs.getLong(1);}}
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO dms_user_role(user_id,role_id) SELECT ?,? FROM dual WHERE NOT EXISTS(SELECT 1 FROM dms_user_role WHERE user_id=? AND role_id=?)")){ps.setLong(1,userId);ps.setLong(2,rid);ps.setLong(3,userId);ps.setLong(4,rid);ps.executeUpdate();}
            }
            try(PreparedStatement ps=c.prepareStatement("UPDATE dms_role_change_request SET status=?,approved_by=?,remarks=?,decided_at=SYSTIMESTAMP WHERE request_id=?")){ps.setString(1,"APPROVE".equals(decision)?"APPROVED":"REJECTED");ps.setLong(2,bossId);ps.setString(3,remarks);ps.setLong(4,requestId);ps.executeUpdate();}
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO dms_notification(recipient_id,type,message,read_flag) VALUES(?,'ROLE_REQUEST',?,'N')")){ps.setLong(1,requester);ps.setString(2,"Role request #"+requestId+" was "+("APPROVE".equals(decision)?"approved":"rejected")+".");ps.executeUpdate();}
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}}
    }
}
