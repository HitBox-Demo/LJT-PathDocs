package com.chekrol.dms.dao;

import com.chekrol.dms.model.Notification;
import com.chekrol.dms.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    public List<Notification> listForUser(long userId)throws SQLException{
        String sql="SELECT notification_id,recipient_id,type,message,document_id,read_flag,created_at FROM dms_notification WHERE recipient_id=? ORDER BY created_at DESC";
        List<Notification> list=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,userId);try(ResultSet rs=ps.executeQuery()){while(rs.next()){Notification n=new Notification();n.setId(rs.getLong(1));n.setRecipientId(rs.getLong(2));n.setType(rs.getString(3));n.setMessage(rs.getString(4));long d=rs.getLong(5);n.setDocumentId(rs.wasNull()?null:d);n.setRead("Y".equals(rs.getString(6)));n.setCreatedAt(rs.getTimestamp(7).toLocalDateTime());list.add(n);}}}return list;
    }
    public void markAllRead(long userId)throws SQLException{try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE dms_notification SET read_flag='Y',read_at=SYSTIMESTAMP WHERE recipient_id=? AND read_flag='N'")){ps.setLong(1,userId);ps.executeUpdate();}}
}
