package com.chekrol.dms.dao;

import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DocumentDAO {
    private static final String SELECT = """
        SELECT doc.document_id, doc.document_code, doc.title, doc.reference_no, doc.sender,
               doc.date_received, doc.category, doc.priority, doc.description,
               doc.destination_department_id, dept.department_name, doc.boss_id, boss.full_name boss_name,
               doc.status, doc.created_by, creator.full_name creator_name, doc.created_at, doc.submitted_at,
               doc.due_date, doc.approved_at, doc.rejection_reason,
               pf.original_name primary_file_name, pf.storage_path primary_file_path
        FROM dms_document doc
        LEFT JOIN dms_department dept ON dept.department_id=doc.destination_department_id
        LEFT JOIN dms_user boss ON boss.user_id=doc.boss_id
        LEFT JOIN dms_user creator ON creator.user_id=doc.created_by
        LEFT JOIN dms_document_file pf ON pf.document_id=doc.document_id AND pf.primary_flag='Y'
        """;

    public List<DocumentRecord> listForUser(User user, String view, String query) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendAccess(sql, params, user);
        switch (view == null ? "repository" : view) {
            case "draft" -> { sql.append(" AND doc.status='DRAFT' AND doc.created_by=?"); params.add(user.getId()); }
            case "pending" -> sql.append(" AND doc.status='PENDING_APPROVAL'");
            case "returned" -> { sql.append(" AND doc.status='RETURNED_FOR_CORRECTION' AND doc.created_by=?"); params.add(user.getId()); }
            case "department" -> sql.append(" AND doc.status='ROUTED'");
            default -> { }
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(doc.document_code) LIKE ? OR LOWER(doc.title) LIKE ? OR LOWER(doc.reference_no) LIKE ? OR LOWER(doc.sender) LIKE ?)");
            String q="%"+query.toLowerCase(Locale.ROOT)+"%"; params.add(q);params.add(q);params.add(q);params.add(q);
        }
        sql.append(" ORDER BY doc.created_at DESC");
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql.toString())) {
            bind(ps,params); try (ResultSet rs=ps.executeQuery()) { return mapDocuments(rs); }
        }
    }

    public DocumentRecord findAuthorized(User user, long id) throws SQLException {
        StringBuilder sql=new StringBuilder(SELECT).append(" WHERE doc.document_id=? ");
        List<Object> params=new ArrayList<>(); params.add(id); appendAccess(sql,params,user);
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql.toString())){
            bind(ps,params);try(ResultSet rs=ps.executeQuery()){List<DocumentRecord> list=mapDocuments(rs);return list.isEmpty()?null:list.get(0);}
        }
    }

    public List<DocumentFile> listFiles(long documentId) throws SQLException {
        String sql="SELECT file_id,document_id,original_name,storage_name,storage_path,mime_type,file_size,primary_flag FROM dms_document_file WHERE document_id=? ORDER BY primary_flag DESC,file_id";
        List<DocumentFile> list=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setLong(1,documentId);try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){DocumentFile f=new DocumentFile();f.setId(rs.getLong(1));f.setDocumentId(rs.getLong(2));f.setOriginalName(rs.getString(3));f.setStorageName(rs.getString(4));f.setStoragePath(rs.getString(5));f.setMimeType(rs.getString(6));f.setFileSize(rs.getLong(7));f.setPrimaryFile("Y".equals(rs.getString(8)));list.add(f);}
            }
        }return list;
    }

    public DocumentFile findFileAuthorized(User user,long fileId) throws SQLException {
        String sql="""
            SELECT f.file_id,f.document_id,f.original_name,f.storage_name,f.storage_path,f.mime_type,f.file_size,f.primary_flag,
                   doc.created_by,doc.boss_id,doc.destination_department_id,doc.status
            FROM dms_document_file f JOIN dms_document doc ON doc.document_id=f.document_id
            WHERE f.file_id=?
            """;
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setLong(1,fileId);
            try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())return null;
                long creator=rs.getLong(9);long boss=rs.getLong(10);long dept=rs.getLong(11);
                boolean allowed=user.hasRole("CLERK")||user.hasRole("SYSTEM_ADMIN")||creator==user.getId()||boss==user.getId()
                        ||(user.hasRole("DEPARTMENT_USER")&&user.getDepartmentId()!=null&&dept==user.getDepartmentId()
                           &&"ROUTED".equals(rs.getString(12))&&(!"Y".equals(rs.getString(13))));
                if(!allowed)return null;
                DocumentFile f=new DocumentFile();f.setId(rs.getLong(1));f.setDocumentId(rs.getLong(2));f.setOriginalName(rs.getString(3));f.setStorageName(rs.getString(4));f.setStoragePath(rs.getString(5));f.setMimeType(rs.getString(6));f.setFileSize(rs.getLong(7));f.setPrimaryFile("Y".equals(rs.getString(8)));return f;
            }
        }
    }

    public long create(DocumentRecord doc,List<DocumentFile> files,boolean submit) throws SQLException {
        String insert="""
          INSERT INTO dms_document(document_code,title,reference_no,sender,date_received,category,priority,description,
          destination_department_id,boss_id,status,created_by,submitted_at,due_date)
          VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
          """;
        String insertFile="INSERT INTO dms_document_file(document_id,original_name,storage_name,storage_path,mime_type,file_size,primary_flag) VALUES(?,?,?,?,?,?,?)";
        String history="INSERT INTO dms_document_history(document_id,user_id,action,new_value,remarks) VALUES(?,?,?,?,?)";
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);try{
                long id;try(PreparedStatement ps=c.prepareStatement(insert,new String[]{"DOCUMENT_ID"})){
                    int i=1;ps.setString(i++,doc.getDocumentCode());ps.setString(i++,doc.getTitle());ps.setString(i++,doc.getReferenceNo());ps.setString(i++,doc.getSender());
                    ps.setDate(i++,java.sql.Date.valueOf(doc.getDateReceived()));ps.setString(i++,doc.getCategory());ps.setString(i++,doc.getPriority());ps.setString(i++,doc.getDescription());
                    ps.setLong(i++,doc.getDestinationDepartmentId());ps.setLong(i++,doc.getBossId());ps.setString(i++,submit?"PENDING_APPROVAL":"DRAFT");ps.setLong(i++,doc.getCreatedById());
                    if(submit)ps.setTimestamp(i++,Timestamp.valueOf(LocalDateTime.now()));else ps.setNull(i++,Types.TIMESTAMP);
                    if(submit)ps.setDate(i++,java.sql.Date.valueOf(doc.getDueDate()));else ps.setNull(i++,Types.DATE);
                    ps.executeUpdate();try(ResultSet keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("Oracle did not return the new document ID.");id=keys.getLong(1);}
                }
                for(DocumentFile f:files){try(PreparedStatement ps=c.prepareStatement(insertFile)){ps.setLong(1,id);ps.setString(2,f.getOriginalName());ps.setString(3,f.getStorageName());ps.setString(4,f.getStoragePath());ps.setString(5,f.getMimeType());ps.setLong(6,f.getFileSize());ps.setString(7,f.isPrimaryFile()?"Y":"N");ps.executeUpdate();}}
                try(PreparedStatement ps=c.prepareStatement(history)){ps.setLong(1,id);ps.setLong(2,doc.getCreatedById());ps.setString(3,submit?"SUBMITTED":"CREATED_DRAFT");ps.setString(4,submit?"PENDING_APPROVAL":"DRAFT");ps.setString(5,null);ps.executeUpdate();}
                if(submit)insertNotification(c,doc.getBossId(),"PENDING_APPROVAL",doc.getDocumentCode()+" is waiting for your approval.",id);
                c.commit();return id;
            }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }

    public Map<String,Long> dashboardStats(User user) throws SQLException {
        Map<String,Long> map=new LinkedHashMap<>();
        for(String key:List.of("TOTAL","PENDING","RETURNED","ROUTED","OVERDUE"))map.put(key,0L);
        List<DocumentRecord> docs=listForUser(user,"repository","");
        map.put("TOTAL",(long)docs.size());
        map.put("PENDING",docs.stream().filter(d->"PENDING_APPROVAL".equals(d.getStatus())).count());
        map.put("RETURNED",docs.stream().filter(d->"RETURNED_FOR_CORRECTION".equals(d.getStatus())).count());
        map.put("ROUTED",docs.stream().filter(d->"ROUTED".equals(d.getStatus())).count());
        map.put("OVERDUE",docs.stream().filter(d->"PENDING_APPROVAL".equals(d.getStatus())&&d.getDueDate()!=null&&d.getDueDate().isBefore(LocalDate.now())).count());
        return map;
    }

    public void recall(User user,long documentId) throws SQLException {
        String update="UPDATE dms_document SET status='RECALLED',updated_at=SYSTIMESTAMP WHERE document_id=? AND created_by=? AND status='PENDING_APPROVAL'";
        String history="INSERT INTO dms_document_history(document_id,user_id,action,old_value,new_value) VALUES(?,?,'RECALLED','PENDING_APPROVAL','RECALLED')";
        try(Connection c=DatabaseConnection.getConnection()){c.setAutoCommit(false);try(PreparedStatement ps=c.prepareStatement(update)){ps.setLong(1,documentId);ps.setLong(2,user.getId());if(ps.executeUpdate()!=1)throw new SQLException("Document cannot be recalled.");try(PreparedStatement h=c.prepareStatement(history)){h.setLong(1,documentId);h.setLong(2,user.getId());h.executeUpdate();}c.commit();}catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}}
    }

    static void insertNotification(Connection c,long recipient,String type,String message,long documentId)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO dms_notification(recipient_id,type,message,document_id,read_flag) VALUES(?,?,?,?, 'N')")){ps.setLong(1,recipient);ps.setString(2,type);ps.setString(3,message);ps.setLong(4,documentId);ps.executeUpdate();}
    }

    private void appendAccess(StringBuilder sql,List<Object> params,User user){
        if(user.hasRole("SYSTEM_ADMIN")||user.hasRole("CLERK"))return;
        if(user.hasRole("BOSS")){sql.append(" AND doc.boss_id=?");params.add(user.getId());return;}
        sql.append(" AND doc.status='ROUTED' AND doc.destination_department_id=?");
        params.add(user.getDepartmentId()==null?-1:user.getDepartmentId());
    }
    private void bind(PreparedStatement ps,List<Object> params)throws SQLException{for(int i=0;i<params.size();i++){Object v=params.get(i);if(v instanceof Long l)ps.setLong(i+1,l);else ps.setString(i+1,String.valueOf(v));}}
    private List<DocumentRecord> mapDocuments(ResultSet rs)throws SQLException{List<DocumentRecord> list=new ArrayList<>();while(rs.next()){DocumentRecord d=new DocumentRecord();
        d.setId(rs.getLong("document_id"));
        d.setDocumentCode(rs.getString("document_code"));
        d.setTitle(rs.getString("title"));
        d.setReferenceNo(rs.getString("reference_no"));
        d.setSender(rs.getString("sender"));
        java.sql.Date dr=rs.getDate("date_received");
        d.setDateReceived(dr==null?null:dr.toLocalDate());
        d.setCategory(rs.getString("category"));
        d.setPriority(rs.getString("priority"));
        d.setConfidential(false);
        d.setDescription(rs.getString("description"));
        long dep=rs.getLong("destination_department_id");
        d.setDestinationDepartmentId(rs.wasNull()?null:dep);
        d.setDestinationDepartmentName(rs.getString("department_name"));
        long boss=rs.getLong("boss_id");
        d.setBossId(rs.wasNull()?null:boss);
        d.setBossName(rs.getString("boss_name"));
        d.setStatus(rs.getString("status"));
        long creator=rs.getLong("created_by");
        d.setCreatedById(rs.wasNull()?null:creator);
        d.setCreatedByName(rs.getString("creator_name"));
        Timestamp t=rs.getTimestamp("created_at");
        d.setCreatedAt(t==null?null:t.toLocalDateTime());
        t=rs.getTimestamp("submitted_at");
        d.setSubmittedAt(t==null?null:t.toLocalDateTime());
        java.sql.Date due=rs.getDate("due_date");
        d.setDueDate(due==null?null:due.toLocalDate());
        t=rs.getTimestamp("approved_at");
        d.setApprovedAt(t==null?null:t.toLocalDateTime());
        d.setRejectionReason(rs.getString("rejection_reason"));
        d.setPrimaryFileName(rs.getString("primary_file_name"));
        d.setPrimaryFilePath(rs.getString("primary_file_path"));
        if(d.getSubmittedAt()!=null&&"PENDING_APPROVAL".equals(d.getStatus()))
            d.setDaysPending((int)ChronoUnit.DAYS.between(d.getSubmittedAt().toLocalDate(),LocalDate.now()));
        list.add(d);
    }
    return list;
}}
